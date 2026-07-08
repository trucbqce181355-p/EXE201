import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { productApi } from "../services/api";
import "../assets/product-detail.css";

const ProductDetailPage = () => {
    const { productId } = useParams();
    const [product, setProduct] = useState<any>(null);
    const [reviews, setReviews] = useState<any[]>([]);
    const [rating, setRating] = useState<number>(5);
    const [comment, setComment] = useState<string>("");
    const [submittingReview, setSubmittingReview] = useState<boolean>(false);
    const [reviewError, setReviewError] = useState<string>("");

    useEffect(() => {
        fetchDetail();
        fetchReviews();
    }, [productId]);

    const fetchDetail = async () => {
        try {
            const res = await productApi.getById(Number(productId));
            setProduct(res?.data);
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

    const handleSubmitReview = async (e: React.FormEvent) => {
        e.preventDefault();
        setReviewError("");
        setSubmittingReview(true);
        try {
            await productApi.createReview(Number(productId), { rating, comment });
            setComment("");
            setRating(5);
            // Refresh product detail and reviews
            fetchDetail();
            fetchReviews();
        } catch (err: any) {
            console.error("Submit review error:", err);
            setReviewError(err?.response?.data?.message || "Failed to submit review. Make sure you are logged in.");
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

    if (!product) return <p className="loading">Đang tải...</p>;

    return (
        <div className="product-detail">

            {/* HEADER */}
            <div className="detail-header">
                <h2>{product.name}</h2>
                <div className="d-flex align-items-center gap-2">
                    {product.status && (
                        <span className={`status ${product.status.toLowerCase()}`}>
                            {product.status}
                        </span>
                    )}
                    {product.averageRating !== undefined && product.averageRating !== null && product.averageRating > 0 && (
                        <div className="rating-badge" style={{ backgroundColor: "#f8f9fa", padding: "4px 8px", borderRadius: "8px", border: "1px solid #dee2e6", display: "inline-flex", alignItems: "center", gap: "5px" }}>
                            {renderStars(product.averageRating)}
                            <span style={{ fontWeight: "bold", fontSize: "0.9rem" }}>{product.averageRating.toFixed(1)}</span>
                            <span style={{ color: "#6c757d", fontSize: "0.8rem" }}>({product.reviewCount || 0})</span>
                        </div>
                    )}
                </div>
            </div>

            {/* BASIC INFO */}
            <div className="detail-card">
                <h3>📦 Thông tin cơ bản</h3>
                <p><strong>SKU:</strong> {product.sku}</p>
                <p><strong>Giá bán:</strong> {(product.price || 0).toLocaleString()} đ</p>
                <p><strong>Mô tả:</strong> {product.description || "Chưa có mô tả"}</p>
                <p>
                    <strong>Trạng thái:</strong> {product.available ? "Đang bán" : "Ngừng bán"}
                </p>
            </div>

            {/* CATEGORY */}
            <div className="detail-card">
                <h3>📂 Danh mục</h3>
                <p><strong>Tên danh mục:</strong> {product.category?.name || "Chưa phân loại"}</p>
                <p><strong>Mô tả:</strong> {product.category?.description || "Không có"}</p>
            </div>

            {/* TAGS */}
            <div className="detail-card">
                <h3>🏷 Nhãn</h3>
                {!product.tags || product.tags.length === 0 ? (
                    <p>Không có nhãn</p>
                ) : (
                    <div className="tag-list">
                        {product.tags.map((t: any, idx: number) => (
                            <span key={idx} className="tag">{t}</span>
                        ))}
                    </div>
                )}
            </div>

            {/* AVAILABILITY */}
            <div className="detail-card">
                <h3>🏪 Khả dụng</h3>
                <p><strong>Bán toàn hệ thống:</strong> {product.availability?.globalAvailable ? "Có" : "Không"}</p>
                <p><strong>Bán từ ngày:</strong> {product.availability?.availableFrom || "Không có"}</p>
                <p><strong>Đến ngày:</strong> {product.availability?.availableUntil || "Không có"}</p>
                <p>
                    <strong>Tự động ngừng bán khi hết kho:</strong>{" "}
                    {product.availability?.autoUnavailableWhenOutOfStock ? "Có" : "Không"}
                </p>
            </div>

            {/* IMAGES */}
            <div className="detail-card">
                <h3>🖼 Hình ảnh</h3>
                {!product.images || product.images.length === 0 ? (
                    <p>Không có hình ảnh</p>
                ) : (
                    <div className="image-list">
                        {product.images.map((img: any) => (
                            <img key={img.id} src={img.imageUrl} alt="" style={{ maxWidth: "150px", height: "auto", borderRadius: "8px", margin: "5px" }} />
                        ))}
                    </div>
                )}
            </div>

            {/* REVIEWS SECTION */}
            <div className="detail-card">
                <h3>💬 Đánh giá từ khách hàng</h3>
                
                {/* List Reviews */}
                {reviews.length === 0 ? (
                    <p style={{ color: "#6c757d", fontStyle: "italic" }}>Chưa có đánh giá nào. Hãy là người đầu tiên đánh giá sản phẩm này!</p>
                ) : (
                    <div className="reviews-list" style={{ display: "flex", flexDirection: "column", gap: "15px", marginBottom: "25px" }}>
                        {reviews.map((review: any) => (
                            <div key={review.id} className="p-3 border rounded" style={{ backgroundColor: "#fdfdfd" }}>
                                <div className="d-flex justify-content-between align-items-center mb-1">
                                    <strong style={{ color: "#495057" }}>{review.userName}</strong>
                                    <small style={{ color: "#6c757d" }}>{new Date(review.createdAt).toLocaleDateString()}</small>
                                </div>
                                <div className="mb-2">
                                    {renderStars(review.rating)}
                                </div>
                                <p className="mb-0" style={{ color: "#212529" }}>{review.comment}</p>
                            </div>
                        ))}
                    </div>
                )}

                {/* Review Form */}
                <form onSubmit={handleSubmitReview} className="mt-4 p-3 border rounded bg-light">
                    <h4>Viết đánh giá của bạn</h4>
                    {reviewError && <div className="alert alert-danger p-2 fs-6">{reviewError}</div>}
                    
                    <div className="mb-3">
                        <label className="form-label font-weight-bold">Điểm số</label>
                        <select className="form-select" value={rating} onChange={(e) => setRating(Number(e.target.value))} style={{ width: "120px" }}>
                            <option value={5}>5 Sao</option>
                            <option value={4}>4 Sao</option>
                            <option value={3}>3 Sao</option>
                            <option value={2}>2 Sao</option>
                            <option value={1}>1 Sao</option>
                        </select>
                    </div>

                    <div className="mb-3">
                        <label className="form-label font-weight-bold">Bình luận</label>
                        <textarea className="form-control" rows={3} placeholder="Chia sẻ trải nghiệm của bạn về sản phẩm này..." value={comment} onChange={(e) => setComment(e.target.value)} required />
                    </div>

                    <button type="submit" className="btn btn-primary" disabled={submittingReview}>
                        {submittingReview ? "Đang gửi..." : "Gửi đánh giá"}
                    </button>
                </form>
            </div>

        </div>
    );
};

export default ProductDetailPage;