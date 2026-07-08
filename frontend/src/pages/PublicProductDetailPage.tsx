import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { productApi, cartApi } from "../services/api";
import Swal from "sweetalert2";
import "../assets/style.css";
import "../assets/product-detail.css";

const fallbackProducts = [
    {
        id: 9901,
        name: "Cảm biến BeAn",
        sku: "BEAN-SEN-01",
        price: 350000,
        description: "Cảm biến hồng ngoại kết hợp cảm biến radar thông minh phát hiện chuyển động trong xe cực kỳ nhạy bén.",
        preparationTime: 5,
        available: true,
        category: { name: "Cảm biến" },
        images: [{ id: 1, imageUrl: "images/menu-1.png", isPrimary: true }],
        status: "ACTIVE",
        averageRating: 4.8,
        reviewCount: 15
    },
    {
        id: 9902,
        name: "Bộ điều khiển BeAn",
        sku: "BEAN-CON-02",
        price: 1200000,
        description: "Điều khiển trung tâm xử lý dữ liệu từ tất cả cảm biến và cảnh báo khẩn cấp qua ứng dụng di động.",
        preparationTime: 10,
        available: true,
        category: { name: "Bộ điều khiển" },
        images: [{ id: 2, imageUrl: "images/menu-2.png", isPrimary: true }],
        status: "ACTIVE",
        averageRating: 4.9,
        reviewCount: 32
    },
    {
        id: 9903,
        name: "Còi cảnh báo BeAn",
        sku: "BEAN-ALA-03",
        price: 250000,
        description: "Còi cảnh báo âm lượng 120dB kích hoạt ngay lập tức khi phát hiện có sự cố ngoài ý muốn.",
        preparationTime: 5,
        available: true,
        category: { name: "Còi báo động" },
        images: [{ id: 3, imageUrl: "images/menu-3.png", isPrimary: true }],
        status: "ACTIVE",
        averageRating: 4.7,
        reviewCount: 8
    }
];

const PublicProductDetailPage = () => {
    const { productId } = useParams();
    const navigate = useNavigate();
    const [product, setProduct] = useState<any>(null);
    const [reviews, setReviews] = useState<any[]>([]);
    const [rating, setRating] = useState<number>(5);
    const [comment, setComment] = useState<string>("");
    const [submittingReview, setSubmittingReview] = useState<boolean>(false);
    const [reviewError, setReviewError] = useState<string>("");
    const [quantity, setQuantity] = useState<number>(1);
    const [loadingCart, setLoadingCart] = useState<boolean>(false);

    useEffect(() => {
        window.scrollTo(0, 0);
        const idNum = Number(productId);
        if (idNum >= 9901 && idNum <= 9903) {
            const found = fallbackProducts.find(p => p.id === idNum);
            setProduct(found);
            setReviews([
                { id: 1, rating: 5, comment: "Sản phẩm dùng cực kỳ ổn định, an tâm hẳn!", createdDate: "2026-07-01", author: "Nguyễn Văn A" },
                { id: 2, rating: 4, comment: "Giao hàng nhanh, đóng gói kỹ càng.", createdDate: "2026-07-02", author: "Trần Thị B" }
            ]);
            return;
        }

        fetchDetail();
        fetchReviews();
    }, [productId]);

    const fetchDetail = async () => {
        try {
            const res = await productApi.getById(Number(productId));
            setProduct(res?.data || res);
        } catch (err) {
            console.error("Fetch detail error:", err);
        }
    };

    const fetchReviews = async () => {
        try {
            const res = await productApi.getReviews(Number(productId));
            setReviews(res?.data || []);
        } catch (err) {
            console.error("Fetch reviews error:", err);
        }
    };

    const handleAddToCart = async () => {
        const loggedInUser = localStorage.getItem("email");
        if (!loggedInUser) {
            Swal.fire({
                title: "Thông báo",
                text: "Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng!",
                icon: "warning",
                confirmButtonColor: "#5d4037"
            });
            return;
        }

        setLoadingCart(true);
        try {
            await cartApi.addItem(product.id, product.name, product.price, quantity);
            Swal.fire({
                title: "Thành công!",
                text: `Đã thêm ${quantity} x ${product.name} vào giỏ hàng!`,
                icon: "success",
                confirmButtonColor: "#5d4037"
            });
        } catch (err) {
            Swal.fire({
                title: "Thất bại",
                text: "Không thể thêm sản phẩm vào giỏ hàng.",
                icon: "error",
                confirmButtonColor: "#5d4037"
            });
        } finally {
            setLoadingCart(false);
        }
    };

    const handleSubmitReview = async (e: React.FormEvent) => {
        e.preventDefault();
        setReviewError("");
        setSubmittingReview(true);
        try {
            await productApi.createReview(Number(productId), { rating, comment });
            setComment("");
            setRating(5);
            fetchDetail();
            fetchReviews();
        } catch (err: any) {
            console.error("Submit review error:", err);
            setReviewError(err?.response?.data?.message || "Không thể gửi đánh giá. Vui lòng kiểm tra đăng nhập.");
        } finally {
            setSubmittingReview(false);
        }
    };

    const renderStars = (score: number) => {
        const stars = [];
        const fullStars = Math.round(score);
        for (let i = 1; i <= 5; i++) {
            stars.push(
                <span key={i} style={{ color: i <= fullStars ? "#ffc107" : "#e4e5e9", fontSize: "1.2rem" }}>
                    ★
                </span>
            );
        }
        return stars;
    };

    if (!product) return <p className="loading" style={{ textAlign: "center", padding: "50px", fontSize: "18px" }}>Đang tải...</p>;

    const placeholders = [
        "images/menu-1.png",
        "images/menu-2.png",
        "images/menu-3.png",
        "images/menu-4.png",
        "images/menu-5.png",
        "images/menu-6.png"
    ];
    const defaultPlaceholder = placeholders[(product.id || 0) % placeholders.length];
    const primaryImage = product.images?.find((img: any) => img.isPrimary)?.imageUrl 
                       || product.images?.[0]?.imageUrl 
                       || defaultPlaceholder;

    return (
        <div style={{ backgroundColor: "#fbf8f6", minHeight: "100vh", fontFamily: "Arial, sans-serif" }}>
            
            {/* Nav Header */}
            <header className="header" style={{ position: "sticky", top: 0, zIndex: 1000, backgroundColor: "#fff", borderBottom: "1px solid #efebe9", padding: "15px 7%" }}>
                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                    <a href="/" className="logo" style={{ color: "#5d4037", fontWeight: "bold", fontSize: "24px", textDecoration: "none" }}>
                        <img src="/images/logo.png" alt="BeAn Logo" style={{ height: "50px", width: "50px", borderRadius: "50%", objectFit: "cover" }} />
                    </a>
                    <div style={{ display: "flex", gap: "20px" }}>
                        <a href="/" style={{ color: "#3e2723", textDecoration: "none", fontWeight: "bold", fontSize: "16px" }}>Trang chủ</a>
                        <a href="/cart" style={{ color: "#3e2723", textDecoration: "none", fontWeight: "bold", fontSize: "16px" }}>Giỏ hàng</a>
                    </div>
                </div>
            </header>

            <div className="container" style={{ maxWidth: "1200px", margin: "40px auto", padding: "0 20px" }}>
                
                {/* Back Link */}
                <button 
                    onClick={() => navigate("/")} 
                    style={{
                        background: "none", border: "none", color: "#5d4037", 
                        cursor: "pointer", display: "flex", alignItems: "center", 
                        gap: "8px", fontWeight: "bold", marginBottom: "25px", fontSize: "16px"
                    }}
                >
                    <i className="fas fa-arrow-left"></i> Quay lại cửa hàng
                </button>

                {/* Main Product Panel */}
                <div style={{ display: "flex", flexWrap: "wrap", gap: "40px", backgroundColor: "#fff", borderRadius: "15px", padding: "40px", boxShadow: "0 10px 30px rgba(0,0,0,0.05)" }}>
                    
                    {/* Left: Product Images */}
                    <div style={{ flex: "1 1 450px", textAlign: "center" }}>
                        <img 
                            src={primaryImage} 
                            alt={product.name} 
                            style={{ maxWidth: "100%", maxHeight: "400px", borderRadius: "10px", objectFit: "cover", boxShadow: "0 8px 16px rgba(0,0,0,0.1)" }} 
                        />
                        {product.images && product.images.length > 1 && (
                            <div style={{ display: "flex", gap: "10px", marginTop: "15px", justifyContent: "center" }}>
                                {product.images.map((img: any) => (
                                    <img key={img.id} src={img.imageUrl} alt="" style={{ width: "80px", height: "60px", borderRadius: "5px", objectFit: "cover", border: "1px solid #ddd" }} />
                                ))}
                            </div>
                        )}
                    </div>

                    {/* Right: Info */}
                    <div style={{ flex: "1 1 500px", display: "flex", flexDirection: "column", gap: "20px" }}>
                        <div>
                            <div style={{ display: "flex", alignItems: "center", gap: "10px", flexWrap: "wrap" }}>
                                <span style={{ backgroundColor: "#efebe9", color: "#5d4037", padding: "4px 10px", borderRadius: "5px", fontSize: "12px", fontWeight: "bold" }}>
                                    {product.category?.name || "Chưa phân loại"}
                                </span>
                                {product.status && (
                                    <span className={`status ${product.status.toLowerCase()}`} style={{ fontSize: "12px" }}>
                                        {product.status}
                                    </span>
                                )}
                            </div>
                            <h1 style={{ color: "#3e2723", fontSize: "32px", fontWeight: "bold", marginTop: "10px", marginBottom: "5px" }}>{product.name}</h1>
                            
                            {product.averageRating > 0 && (
                                <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                                    {renderStars(product.averageRating)}
                                    <span style={{ fontWeight: "bold" }}>{product.averageRating.toFixed(1)}</span>
                                    <span style={{ color: "#777" }}>({product.reviewCount || 0} đánh giá)</span>
                                </div>
                            )}
                        </div>

                        <div style={{ borderTop: "1px solid #efebe9", borderBottom: "1px solid #efebe9", padding: "15px 0" }}>
                            <span style={{ fontSize: "28px", fontWeight: "bold", color: "#5d4037" }}>
                                {(product.price || 0).toLocaleString()} đ
                            </span>
                        </div>

                        <div style={{ color: "#4e342e", lineHeight: "1.6" }}>
                            <p><strong>SKU:</strong> #{product.sku}</p>
                            {product.tags && product.tags.length > 0 && (
                                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px', marginTop: '10px', alignItems: 'center' }}>
                                    <strong>Tags:</strong>
                                    {product.tags.map((tag: string, idx: number) => (
                                        <span key={idx} style={{
                                            backgroundColor: '#eefeeb',
                                            color: '#2e7d32',
                                            border: '1px solid #2e7d32',
                                            padding: '2px 8px',
                                            borderRadius: '4px',
                                            fontSize: '1.2rem',
                                            fontWeight: 'bold'
                                        }}>{tag}</span>
                                    ))}
                                </div>
                            )}
                            <p style={{ marginTop: "10px" }}>{product.description || "Không có mô tả chi tiết cho sản phẩm này."}</p>
                        </div>

                        {/* Add to Cart Actions */}
                        {product.available ? (
                            <div style={{ display: "flex", alignItems: "center", gap: "20px", marginTop: "10px", flexWrap: "wrap" }}>
                                <div style={{ display: "inline-flex", border: "1px solid #bcaaa4", borderRadius: "8px", overflow: "hidden" }}>
                                    <button 
                                        type="button" 
                                        onClick={() => setQuantity(q => Math.max(1, q - 1))} 
                                        style={{ border: "none", backgroundColor: "#fff", width: "40px", height: "40px", fontSize: "18px", cursor: "pointer", color: "#5d4037" }}
                                    >-</button>
                                    <span style={{ display: "inline-flex", alignItems: "center", justifyContent: "center", width: "40px", fontWeight: "bold" }}>{quantity}</span>
                                    <button 
                                        type="button" 
                                        onClick={() => setQuantity(q => q + 1)} 
                                        style={{ border: "none", backgroundColor: "#fff", width: "40px", height: "40px", fontSize: "18px", cursor: "pointer", color: "#5d4037" }}
                                    >+</button>
                                </div>
                                <button 
                                    onClick={handleAddToCart}
                                    disabled={loadingCart}
                                    style={{
                                        border: "none", backgroundColor: "#5d4037", color: "#fff",
                                        padding: "12px 30px", borderRadius: "8px", cursor: "pointer",
                                        fontWeight: "bold", fontSize: "16px", display: "flex", alignItems: "center", gap: "10px"
                                    }}
                                >
                                    <i className="fas fa-shopping-cart"></i> {loadingCart ? "Đang thêm..." : "Thêm vào giỏ hàng"}
                                </button>
                            </div>
                        ) : (
                            <div style={{ color: "#dc3545", fontWeight: "bold", fontSize: "18px" }}>
                                ❌ Tạm thời hết hàng
                            </div>
                        )}
                    </div>
                </div>

                {/* Reviews Section */}
                <div style={{ marginTop: "40px", backgroundColor: "#fff", borderRadius: "15px", padding: "40px", boxShadow: "0 10px 30px rgba(0,0,0,0.05)" }}>
                    <h3 style={{ color: "#3e2723", marginBottom: "25px", borderBottom: "2px solid #efebe9", paddingBottom: "10px" }}>💬 Đánh giá từ khách hàng</h3>

                    {/* Submit Review */}
                    <form onSubmit={handleSubmitReview} style={{ marginBottom: "30px", backgroundColor: "#fdfbf7", padding: "20px", borderRadius: "10px", border: "1px solid #efebe9" }}>
                        <h4 style={{ color: "#5d4037", marginBottom: "15px" }}>Viết đánh giá của bạn</h4>
                        {reviewError && <p style={{ color: "#dc3545", marginBottom: "15px" }}>{reviewError}</p>}
                        
                        <div style={{ display: "flex", alignItems: "center", gap: "15px", marginBottom: "15px" }}>
                            <span>Số sao:</span>
                            <div style={{ display: "flex", gap: "5px" }}>
                                {[1, 2, 3, 4, 5].map((star) => (
                                    <span 
                                        key={star} 
                                        onClick={() => setRating(star)} 
                                        style={{ cursor: "pointer", color: star <= rating ? "#ffc107" : "#e4e5e9", fontSize: "1.5rem" }}
                                    >
                                        ★
                                    </span>
                                ))}
                            </div>
                        </div>

                        <textarea 
                            value={comment}
                            onChange={(e) => setComment(e.target.value)}
                            placeholder="Nhập nhận xét của bạn về sản phẩm..."
                            required
                            style={{ width: "100%", height: "100px", padding: "10px", borderRadius: "5px", border: "1px solid #ccc", marginBottom: "15px", fontFamily: "inherit" }}
                        />

                        <button 
                            type="submit" 
                            disabled={submittingReview}
                            style={{ border: "none", backgroundColor: "#5d4037", color: "#fff", padding: "10px 20px", borderRadius: "5px", cursor: "pointer", fontWeight: "bold" }}
                        >
                            {submittingReview ? "Đang gửi..." : "Gửi đánh giá"}
                        </button>
                    </form>

                    {/* Review List */}
                    <div style={{ display: "flex", flexDirection: "column", gap: "20px" }}>
                        {reviews.length > 0 ? (
                            reviews.map((rev) => {
                                const authorName = rev.userName || rev.author || "Khách hàng ẩn danh";
                                const reviewDate = rev.createdAt ? new Date(rev.createdAt).toLocaleDateString("vi-VN") : rev.createdDate || "—";
                                return (
                                    <div key={rev.id} style={{ borderBottom: "1px solid #f3eff2", paddingBottom: "15px" }}>
                                        <div style={{ display: "flex", justifyContent: "space-between", marginBottom: "5px" }}>
                                            <strong style={{ color: "#3e2723" }}>{authorName}</strong>
                                            <span style={{ color: "#999", fontSize: "13px" }}>{reviewDate}</span>
                                        </div>
                                        <div style={{ marginBottom: "8px" }}>{renderStars(rev.rating)}</div>
                                        <p style={{ color: "#5d4037", margin: 0 }}>{rev.comment}</p>
                                    </div>
                                );
                            })
                        ) : (
                            <p style={{ color: "#777", fontStyle: "italic" }}>Chưa có đánh giá nào cho sản phẩm này.</p>
                        )}
                    </div>
                </div>

            </div>
        </div>
    );
};

export default PublicProductDetailPage;
