/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.group1.engagement_service.service;

import com.group1.engagement_service.dto.CartDTO;
import com.group1.engagement_service.dto.CartMapper;
import com.group1.engagement_service.exception.BadRequestException;
import com.group1.engagement_service.entity.Cart;
import com.group1.engagement_service.entity.CartCoupon;
import com.group1.engagement_service.entity.CartItem;
import com.group1.engagement_service.entity.Coupon;
import com.group1.engagement_service.entity.CouponStatus;
import com.group1.engagement_service.entity.CouponUsage;
import com.group1.engagement_service.entity.Promotion;
import com.group1.engagement_service.entity.PromotionType;
import com.group1.engagement_service.repository.CartItemRepository;
import com.group1.engagement_service.repository.CartRepository;
import com.group1.engagement_service.repository.CouponRepository;
import com.group1.engagement_service.repository.CouponUsageRepository;
import com.group1.engagement_service.request.ApplyCouponRequest;
import com.group1.engagement_service.response.CartResponse;
import com.group1.engagement_service.response.CustomerResponse;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CartService {

    private final RestTemplate restTemplate;
    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final CouponService couponService;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartMapper cartMapper;

    public CartService(RestTemplate restTemplate, CouponRepository couponRepository, CouponUsageRepository couponUsageRepository, CouponService couponService, CartRepository cartRepository, CartItemRepository cartItemRepository, CartMapper cartMapper) {
        this.restTemplate = restTemplate;
        this.couponRepository = couponRepository;
        this.couponUsageRepository = couponUsageRepository;
        this.couponService = couponService;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.cartMapper = cartMapper;
    }

    public Long getCustomerIdByUserId(Long userId, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        String url = "http://localhost:8082/customers/find-id/" + userId;

        try {
            System.out.println("[CartService] Requesting customerId from: " + url);
            ResponseEntity<CustomerResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    CustomerResponse.class
            );
            CustomerResponse body = response.getBody();
            System.out.println("[CartService] Response body: " + body);
            if (body == null || body.getId() == null) {
                System.out.println("[CartService] Body or ID is null!");
                throw new BadRequestException("Cannot get customerId");
            }
            return body.getId();
        } catch (Exception e) {
            System.out.println("[CartService] Error calling customer-service: " + e.getMessage());
            e.printStackTrace();
            throw new BadRequestException("Không thể lấy thông tin khách hàng từ customer-service: " + e.getMessage());
        }
    }

    @Value("${app.promotion.allow-multiple-coupons:false}")
    private boolean allowMultipleCoupons;

    @Transactional
    public CartDTO applyCoupon(Long userId, ApplyCouponRequest request, String token) {
        // 1. Lấy customerId từ userId (Giữ nguyên logic gọi service khác)
        Long customerId = getCustomerIdByUserId(userId, token);

        // 2. Tìm GIỎ HÀNG GỐC (ENTITY) từ Database - KHÔNG dùng DTO ở đây
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new BadRequestException("Giỏ hàng không tồn tại đại ca ơi!"));

        String code = request.getCouponCode();

        // 3. Kiểm tra nếu không cho phép dùng nhiều mã (Clear list cũ nếu cần)
        // Chỗ này thao tác trực tiếp trên list Entity
        if (!allowMultipleCoupons && !cart.getAppliedCoupons().isEmpty()) {
            cart.getAppliedCoupons().clear();
        }

        // 4. Tìm Coupon trong DB
        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new BadRequestException("Mã giảm giá không tồn tại!"));

        // 5. Validate (Đại ca truyền Entity cart vào hàm này nhé)
        validateCoupon(coupon, customerId, cart);

        // 6. Tránh áp dụng trùng chính cái mã đó
        boolean isAlreadyApplied = cart.getAppliedCoupons().stream()
                .anyMatch(cc -> cc.getCoupon().getCode().equals(code));
        if (isAlreadyApplied) {
            throw new BadRequestException("Mã này đại ca đã dùng cho giỏ hàng này rồi!");
        }

        // 7. Tính toán số tiền giảm (Viết hàm nhận Entity Cart)
        BigDecimal newDiscountAmount = calculateDiscount(coupon, cart);

        // 8. Tạo bản ghi trung gian CartCoupon (Entity kết nối với Entity)
        CartCoupon cartCoupon = CartCoupon.builder()
                .cart(cart) // Truyền Entity vào đây
                .coupon(coupon)
                .discountValue(newDiscountAmount)
                .appliedAt(LocalDateTime.now())
                .build();

        // 9. Thêm vào danh sách và tính toán lại tiền bạc
        cart.getAppliedCoupons().add(cartCoupon);

        // Tính tổng Discount từ danh sách Entity CartCoupon
        BigDecimal totalDiscount = cart.getAppliedCoupons().stream()
                .map(CartCoupon::getDiscountValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cart.setDiscountAmount(totalDiscount);

        // Đảm bảo không bị âm tiền nếu discount quá lớn
        BigDecimal finalAmount = cart.getTotalAmount().subtract(totalDiscount);
        cart.setFinalAmount(finalAmount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : finalAmount);

        // Lưu Entity vào DB
        Cart savedCart = cartRepository.save(cart);

        // 10. CHỐT HẠ: Dùng Mapper chuyển Entity vừa lưu thành DTO để trả về cho React
        return cartMapper.toDTO(savedCart);
    }

    @Transactional
    public CartDTO removeCoupon(Long userId, String token) {
        // 1. Lấy customerId từ userId
        Long customerId = getCustomerIdByUserId(userId, token);

        // 2. Tìm giỏ hàng (Entity)
        // Lưu ý: Đảm bảo hàm getCartByCustomer này trả về Entity Cart nhé đại ca
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy giỏ hàng để xóa mã, đại ca ơi!"));

        // 3. Xóa sạch danh sách mã áp dụng
        // Hibernate sẽ tự động xóa các bản ghi trong bảng cart_coupons nhờ orphanRemoval = true hoặc CascadeType.ALL
        cart.getAppliedCoupons().clear();

        // 4. Reset các con số về ban đầu
        cart.setDiscountAmount(BigDecimal.ZERO);
        cart.setFinalAmount(cart.getTotalAmount()); // Final = Total vì đã hết giảm giá

        // 5. Lưu lại Entity
        Cart savedCart = cartRepository.save(cart);

        // 6. TRẢ VỀ DTO để React cập nhật lại tiền trên màn hình ngay lập tức
        return cartMapper.toDTO(savedCart);
    }

    private void validateCoupon(Coupon coupon, Long customerId, Cart cart) {

        // 1. Status
        if (coupon.getStatus() != CouponStatus.ACTIVE) {
            throw new BadRequestException("Coupon has expired");
        }

        // 2. Max uses
        if (coupon.getMaxUses() != null
                && coupon.getTimesUsed() >= coupon.getMaxUses()) {
            throw new BadRequestException("Coupon usage limit reached");
        }

        // 3. Customer usage (1 user xài mấy lần)
        boolean used = couponUsageRepository
                .existsByCouponIdAndCustomerId(coupon.getId(), customerId);

        if (used) {
            throw new BadRequestException("You have already used this coupon");
        }

        // 4. Min order amount (từ Promotion)
        BigDecimal min = coupon.getPromotion().getMinOrderAmount();

        if (min != null && cart.getTotalAmount().compareTo(min) < 0) {
            throw new BadRequestException("Minimum order amount not met");
        }
    }

    @Transactional
    public CartDTO getCartByCustomer(Long customerId) {

        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Giỏ hàng không tồn tại"));
        return cartMapper.toDTO(cart);
    }

    @Transactional
    public CartDTO getCartByCustomerId(Long customerId) {
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Giỏ hàng không tồn tại"));
        return cartMapper.toDTO(cart);
    }

    private BigDecimal calculateDiscount(Coupon coupon, Cart cart) {

        Promotion promo = coupon.getPromotion();

        BigDecimal total = cart.getTotalAmount();
        BigDecimal discount;

        if (promo.getType() == PromotionType.PERCENTAGE_DISCOUNT) {

            discount = total.multiply(promo.getValue())
                    .divide(BigDecimal.valueOf(100));

        } else {
            discount = promo.getValue();
        }

        // max discount
        if (promo.getMaxDiscountAmount() != null
                && discount.compareTo(promo.getMaxDiscountAmount()) > 0) {

            discount = promo.getMaxDiscountAmount();
        }

        return discount;
    }

    @Transactional
    public CartDTO updateItemQuantity(Long itemId, Integer newQuantity) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new BadRequestException("Item not found in cart!"));

        Cart cart = item.getCart();

        if (newQuantity <= 0) {
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(newQuantity);
            item.setSubtotal(item.getPrice().multiply(BigDecimal.valueOf(newQuantity)));
            cartItemRepository.save(item);
        }

        BigDecimal totalAmount = cart.getItems().stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotalAmount(totalAmount);

        revalidateAndApplyCoupons(cart);

        Cart saved = cartRepository.save(cart);
        return cartMapper.toDTO(saved);
    }

    private void revalidateAndApplyCoupons(Cart cart) {
        if (cart.getAppliedCoupons() == null || cart.getAppliedCoupons().isEmpty()) {
            cart.setDiscountAmount(BigDecimal.ZERO);
            cart.setFinalAmount(cart.getTotalAmount());
            return;
        }

        BigDecimal totalDiscount = BigDecimal.ZERO;
        var iterator = cart.getAppliedCoupons().iterator();

        while (iterator.hasNext()) {
            CartCoupon cartCoupon = iterator.next();
            Promotion promo = cartCoupon.getCoupon().getPromotion();

            if (cart.getTotalAmount().compareTo(promo.getMinOrderAmount()) < 0) {
                iterator.remove();
                continue;
            }

            BigDecimal discountOfThisCoupon = BigDecimal.ZERO;
            if (promo.getType() == PromotionType.PERCENTAGE_DISCOUNT) {
                discountOfThisCoupon = cart.getTotalAmount()
                        .multiply(promo.getValue())
                        .divide(BigDecimal.valueOf(100));

                if (promo.getMaxDiscountAmount() != null
                        && discountOfThisCoupon.compareTo(promo.getMaxDiscountAmount()) > 0) {
                    discountOfThisCoupon = promo.getMaxDiscountAmount();
                }
            } else {
                discountOfThisCoupon = promo.getValue();
            }

            cartCoupon.setDiscountValue(discountOfThisCoupon);
            totalDiscount = totalDiscount.add(discountOfThisCoupon);
        }

        cart.setDiscountAmount(totalDiscount);
        BigDecimal finalAmount = cart.getTotalAmount().subtract(totalDiscount);

        cart.setFinalAmount(finalAmount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : finalAmount);
    }

    private void recalculateCartTotal(Cart cart) {
        // Tính tổng tiền gốc từ tất cả items
        BigDecimal totalAmount = cart.getItems().stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cart.setTotalAmount(totalAmount);

        // Giả sử discountAmount đại ca đang để mặc định hoặc tính từ coupon
        // Ở đây mình cập nhật finalAmount = totalAmount - discountAmount
        BigDecimal discount = cart.getDiscountAmount() != null ? cart.getDiscountAmount() : BigDecimal.ZERO;
        cart.setFinalAmount(totalAmount.subtract(discount));
    }

    @Transactional
    public CartDTO removeSpecificCoupon(Long userId, String couponCode, String token) {
        Long customerId = getCustomerIdByUserId(userId, token);
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy giỏ hàng!"));

        // 1. Tìm và xóa đúng mã couponCode trong danh sách Entity
        // Dùng removeIf để xóa chính xác mã code đó
        cart.getAppliedCoupons().removeIf(cc -> cc.getCoupon().getCode().equals(couponCode));

        // 2. Tính toán lại tổng tiền giảm giá dựa trên những mã còn lại
        BigDecimal totalDiscount = cart.getAppliedCoupons().stream()
                .map(CartCoupon::getDiscountValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cart.setDiscountAmount(totalDiscount);

        // 3. Cập nhật lại số tiền cuối cùng
        BigDecimal finalAmount = cart.getTotalAmount().subtract(totalDiscount);
        cart.setFinalAmount(finalAmount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : finalAmount);

        Cart savedCart = cartRepository.save(cart);
        return cartMapper.toDTO(savedCart);
    }

    @Transactional
    public CartDTO clearCart(Long userId, String token) {
        Long customerId = getCustomerIdByUserId(userId, token);
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new BadRequestException("Giỏ hàng không tồn tại"));
        
        cart.getItems().clear();
        cart.getAppliedCoupons().clear();
        cart.setTotalAmount(BigDecimal.ZERO);
        cart.setDiscountAmount(BigDecimal.ZERO);
        cart.setFinalAmount(BigDecimal.ZERO);
        
        Cart savedCart = cartRepository.save(cart);
        return cartMapper.toDTO(savedCart);
    }

    @Transactional
    public CartDTO addItemToCart(Long userId, Long productId, String productName, BigDecimal price, Integer quantity, String token) {
        Long customerId = getCustomerIdByUserId(userId, token);
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setCustomerId(customerId);
                    newCart.setTotalAmount(BigDecimal.ZERO);
                    newCart.setDiscountAmount(BigDecimal.ZERO);
                    newCart.setFinalAmount(BigDecimal.ZERO);
                    return cartRepository.save(newCart);
                });

        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            existingItem.setSubtotal(existingItem.getPrice().multiply(BigDecimal.valueOf(existingItem.getQuantity())));
            cartItemRepository.save(existingItem);
        } else {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProductId(productId);
            item.setProductName(productName);
            item.setPrice(price);
            item.setQuantity(quantity);
            item.setSubtotal(price.multiply(BigDecimal.valueOf(quantity)));
            cartItemRepository.save(item);
            cart.getItems().add(item);
        }

        recalculateCartTotal(cart);
        revalidateAndApplyCoupons(cart);
        Cart saved = cartRepository.save(cart);
        return cartMapper.toDTO(saved);
    }
}
