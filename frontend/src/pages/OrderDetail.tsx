import React, { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import axios from 'axios';
import { customerApi } from '../services/api';
import '../assets/order-history.css';
import '@fortawesome/fontawesome-free/css/all.min.css';

interface OrderItem {
    productName: string;
    quantity: number;
    price: number;
}

interface StatusHistory {
    status: string;
    updatedAt: string;
}

interface OrderDetailData {
    orderId: number;
    items: OrderItem[];
    paymentInfo: {
        method: string;
        status: string;
        paidAt: string;
    };
    deliveryInfo: {
        address: string;
        receiverName: string;
        phone: string;
        status: string;
    };
    statusHistory: StatusHistory[];
}
const savedProfile = localStorage.getItem('accountProfile');
let userId = null;

if (savedProfile) {
    const profile = JSON.parse(savedProfile);
    console.log("Toàn bộ profile nè đại ca:", profile);
    userId = profile.id; // Lấy id từ object đã lưu
}
const API_BASE_URL = 'http://localhost:8082';

const OrderDetail: React.FC = () => {
    const navigate = useNavigate();
    const { orderId } = useParams<{ orderId: string }>();
    const [order, setOrder] = useState<OrderDetailData | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    const formatCurrency = (num: number) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(num);
    const formatDate = (str: string) => str ? new Date(str).toLocaleString('vi-VN') : 'N/A';

    useEffect(() => {
        const fetchOrderDetail = async () => {
            const token = localStorage.getItem('accessToken');
            if (!token) {
                navigate('/');
                return;
            }

            try {
                setLoading(true);
                const payload = JSON.parse(atob(token.split(".")[1]));
                const customerResp = await customerApi.getByUserId(payload.userId);
                const customerId = customerResp?.id || customerResp?.data?.id;

                if (!customerId || customerId === "null") {
                    setError("Không tìm thấy thông tin khách hàng.");
                    setLoading(false);
                    return;
                }

                const response = await axios.get(`${API_BASE_URL}/customer/${customerId}/orders/${orderId}`, {
                    headers: { Authorization: `Bearer ${token}` }
                });
                setOrder(response.data.data || response.data);
                setError(null);
            } catch (err: any) {
                setError(err.response?.data?.message || "Không thể tải thông tin đơn hàng.");
            } finally {
                setLoading(false);
            }
        };

        if (orderId) fetchOrderDetail();
    }, [orderId, navigate]);

    const handlePayOrder = async () => {
        if (!order) return;
        const token = localStorage.getItem('accessToken');
        if (!token) return;

        try {
            const response = await axios.get(`${API_BASE_URL}/api/payment/vnpay/create-url?orderId=${order.orderId}`, {
                headers: { Authorization: `Bearer ${token}` }
            });
            
            if (response.data.success && response.data.data) {
                // Redirect user to VNPay
                window.location.href = response.data.data;
            } else {
                toast.error("Không thể tạo URL thanh toán");
            }
        } catch (error) {
            console.error("Error creating VNPay URL:", error);
            toast.error("Thanh toán thất bại, vui lòng thử lại!");
        }
    };

    const handleCancelOrder = async () => {
        const reason = prompt("Lý do hủy đơn hàng:");
        if (!reason) return;
        
        const token = localStorage.getItem('accessToken');
        const payload = JSON.parse(atob(token!.split(".")[1]));
        const customerResp = await customerApi.getByUserId(payload.userId);
        const customerId = customerResp?.id || customerResp?.data?.id;

        try {
            await axios.put(`${API_BASE_URL}/customer/${customerId}/orders/${orderId}/cancel`, { reason }, {
                headers: { Authorization: `Bearer ${token}` }
            });
            alert('Hủy đơn hàng thành công!');
            window.location.reload();
        } catch (err: any) {
            alert(err.response?.data?.message || 'Lỗi hủy đơn');
        }
    };

    const handleUpdateStatus = async (newStatus: string) => {
        const token = localStorage.getItem('accessToken');
        const payload = JSON.parse(atob(token!.split(".")[1]));
        const customerResp = await customerApi.getByUserId(payload.userId);
        const customerId = customerResp?.id || customerResp?.data?.id;

        try {
            await axios.put(`${API_BASE_URL}/customer/${customerId}/orders/${orderId}/status`, { newStatus, note: 'Update for testing' }, {
                headers: { Authorization: `Bearer ${token}` }
            });
            alert(`Cập nhật trạng thái thành ${newStatus} thành công!`);
            window.location.reload();
        } catch (err: any) {
            alert(err.response?.data?.message || 'Lỗi cập nhật trạng thái');
        }
    };

    if (loading) {
        return (
            <div className="account-page">
                <section className="account-page-shell" style={{ textAlign: 'center', padding: '20rem 0' }}>
                    <div style={{ fontSize: '2rem', color: '#512a10' }}>
                        <i className="fas fa-spinner fa-spin"></i>
                    </div>
                </section>
            </div>
        );
    }

    if (error || !order) {
        return (
            <div className="account-page">
                <section className="account-page-shell" style={{ textAlign: 'center', padding: '20rem 0' }}>
                    <div style={{ fontSize: '2rem', color: 'red' }}>{error}</div>
                    <button onClick={() => navigate(-1)} className="link-btn" style={{ marginTop: '2rem' }}>back</button>
                </section>
            </div>
        );
    }

    const getPaymentStatusLabel = (status: string) => {
        if (status === 'PENDING') return 'Chờ thanh toán';
        if (status === 'PAID') return 'Đã thanh toán';
        return status;
    };

    const getTimelineStatusLabel = (status: string) => {
        switch (status) {
            case 'Pending': return 'Chờ xử lý';
            case 'Completed': return 'Hoàn thành';
            case 'Cancelled': return 'Đã hủy';
            case 'Processing': return 'Đang xử lý';
            case 'Shipped': return 'Đang giao hàng';
            default: return status;
        }
    };

    const currentStatus = [...order.statusHistory].sort((a,b) => new Date(a.updatedAt).getTime() - new Date(b.updatedAt).getTime()).pop()?.status || 'Pending';

    return (
        <div className="account-page">
            <header className="header active">
                <div className="container d-flex align-items-center w-100">
                    <Link to="/" className="logo"><img src="/images/logo.png" alt="BeAn Logo" style={{ height: "50px", width: "50px", borderRadius: "50%", objectFit: "cover" }} /></Link>
                    <div className="account-page-actions">
                        <button type="button" className="link-btn account-top-btn" onClick={() => navigate(-1)}>
                            <i className="fas fa-arrow-left"></i> <span>Quay lại</span>
                        </button>
                    </div>
                </div>
            </header>

            <section className="account-page-shell" style={{ padding: '10rem 0', display: 'block', width: '100%' }}>
                <div style={{ width: '100%', display: 'flex', justifyContent: 'center', padding: '0 2rem' }}>
                    <div className="account-card shadow-sm" style={{ background: '#fff', borderRadius: '1.5rem', width: '95%', maxWidth: '1200px', padding: '4rem', margin: '0 auto' }}>
                        
                        <div className="detail-header" style={{ borderBottom: '2px solid #512a10', paddingBottom: '2rem', marginBottom: '3rem', display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end' }}>
                            <div>
                                <span className="account-eyebrow">Chi tiết hóa đơn</span>
                                <h3 style={{ fontSize: '3rem', marginTop: '0.5rem' }}>Đơn hàng #ORD-2026-{order.orderId}</h3>
                            </div>
                            <span style={{ fontSize: '1.6rem', color: '#888' }}>
                                Đặt lúc: {order.statusHistory.length > 0 ? formatDate(order.statusHistory[0].updatedAt) : 'N/A'}
                            </span>
                        </div>

                        {/* Action Buttons */}
                        <div style={{ display: 'flex', gap: '1rem', marginBottom: '3rem', flexWrap: 'wrap' }}>
                            {order.paymentInfo.status === 'PENDING' && currentStatus !== 'Cancelled' && (
                                <button onClick={handlePayOrder} className="link-btn" style={{ background: '#2e7d32', color: 'white' }}>
                                    <i className="fas fa-money-bill-wave"></i> Thanh toán ngay
                                </button>
                            )}
                            {(currentStatus === 'Pending' || currentStatus === 'Processing') && (
                                <button onClick={handleCancelOrder} className="link-btn" style={{ background: '#c62828', color: 'white' }}>
                                    <i className="fas fa-times"></i> Hủy đơn hàng
                                </button>
                            )}
                            {/* For Testing Status Updates */}
                            {currentStatus !== 'Completed' && currentStatus !== 'Cancelled' && (
                                <>
                                    <button onClick={() => handleUpdateStatus('Processing')} className="link-btn" style={{ background: '#f57c00', color: 'white' }}>
                                        Processing
                                    </button>
                                    <button onClick={() => handleUpdateStatus('Shipped')} className="link-btn" style={{ background: '#0277bd', color: 'white' }}>
                                        Shipped
                                    </button>
                                    <button onClick={() => handleUpdateStatus('Completed')} className="link-btn" style={{ background: '#388e3c', color: 'white' }}>
                                        Completed
                                    </button>
                                </>
                            )}
                        </div>

                        <div style={{ display: 'flex', flexDirection: 'column', gap: '4rem' }}>
                            
                            <div style={{ width: '100%', borderBottom: '1px solid #eee', paddingBottom: '2rem' }}>
                                <h4 style={{ fontSize: '1.8rem', color: '#512a10', marginBottom: '1.5rem', textTransform: 'uppercase', display: 'flex', alignItems: 'center', gap: '1rem' }}>
                                    <i className="fas fa-truck"></i> Địa chỉ giao hàng
                                </h4>
                                <div style={{ paddingLeft: '2.8rem' }}>
                                    <p style={{ fontSize: '1.7rem', fontWeight: 'bold' }}>{order.deliveryInfo?.receiverName || "Pham Duc Khang"}</p>
                                    <p style={{ fontSize: '1.6rem', color: '#666', marginTop: '0.5rem' }}>{order.deliveryInfo?.phone || "0904567788"}</p>
                                    <p style={{ fontSize: '1.6rem', color: '#666', lineHeight: '1.6' }}>{order.deliveryInfo?.address || "15 Vo Van Tan, Ward 6, District 3, Ho Chi Minh City"}</p>
                                </div>
                            </div>

                            <div style={{ width: '100%', borderBottom: '1px solid #eee', paddingBottom: '2rem' }}>
                                <h4 style={{ fontSize: '1.8rem', color: '#512a10', marginBottom: '1.5rem', textTransform: 'uppercase', display: 'flex', alignItems: 'center', gap: '1rem' }}>
                                    <i className="fas fa-credit-card"></i> Phương thức thanh toán
                                </h4>
                                <div style={{ paddingLeft: '2.8rem' }}>
                                    <p style={{ fontSize: '1.6rem' }}>Phương thức: <strong>{order.paymentInfo.method}</strong></p>
                                    <p style={{ fontSize: '1.6rem', marginTop: '0.5rem' }}>Trạng thái: <span style={{ color: '#2e7d32', fontWeight: 'bold' }}>{getPaymentStatusLabel(order.paymentInfo.status)}</span></p>
                                    <p style={{ fontSize: '1.5rem', color: '#888', marginTop: '0.5rem' }}>Thanh toán lúc: {formatDate(order.paymentInfo.paidAt)}</p>
                                </div>
                            </div>

                            <div style={{ width: '100%' }}>
                                <h4 style={{ fontSize: '1.8rem', color: '#512a10', marginBottom: '1.5rem', textTransform: 'uppercase', display: 'flex', alignItems: 'center', gap: '1rem' }}>
                                    <i className="fas fa-coffee"></i> Sản phẩm đã đặt
                                </h4>
                                <div style={{ background: '#f8f9fa', borderRadius: '1rem', padding: '3rem', width: '100%' }}>
                                    {order.items.map((item, idx) => (
                                        <div key={idx} style={{ display: 'flex', justifyContent: 'space-between', padding: '1.5rem 0', borderBottom: idx === order.items.length - 1 ? 'none' : '1px solid #ddd' }}>
                                            <div style={{ fontSize: '1.7rem' }}>
                                                <span style={{ fontWeight: '500' }}>{item.productName}</span>
                                                <span style={{ color: '#888', marginLeft: '1.5rem' }}>x{item.quantity}</span>
                                            </div>
                                            <span style={{ fontWeight: 'bold', fontSize: '1.7rem' }}>{formatCurrency(item.price * item.quantity)}</span>
                                        </div>
                                    ))}
                                    <div style={{ borderTop: '2px solid #512a10', marginTop: '2rem', paddingTop: '2rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                        <span style={{ fontSize: '2.2rem', fontWeight: 'bold' }}>Tổng tiền</span>
                                        <span style={{ fontSize: '2.4rem', fontWeight: 'bold', color: '#512a10' }}>
                                            {formatCurrency(order.items.reduce((acc, item) => acc + (item.price * item.quantity), 0))}
                                        </span>
                                    </div>
                                </div>
                            </div>

                            <div style={{ width: '100%', marginTop: '2rem' }}>
                                <h4 style={{ fontSize: '1.8rem', color: '#512a10', marginBottom: '2.5rem', textTransform: 'uppercase', display: 'flex', alignItems: 'center', gap: '1rem' }}>
                                    <i className="fas fa-history"></i> Lịch trình đơn hàng
                                </h4>
                                <div style={{ paddingLeft: '3rem', borderLeft: '3px solid #be9c79', marginLeft: '1rem' }}>
                                    {order.statusHistory.sort((a,b) => new Date(a.updatedAt).getTime() - new Date(b.updatedAt).getTime()).map((step, idx) => (
                                        <div key={idx} style={{ position: 'relative', marginBottom: '3rem' }}>
                                            <div style={{ position: 'absolute', left: '-3.8rem', top: '0.2rem', width: '1.5rem', height: '1.5rem', borderRadius: '50%', background: '#512a10', border: '3px solid #fff', boxShadow: '0 0 0 2px #be9c79' }}></div>
                                            <p style={{ fontSize: '1.6rem', fontWeight: 'bold', margin: '0' }}>{getTimelineStatusLabel(step.status)}</p>
                                            <p style={{ fontSize: '1.4rem', color: '#888', margin: '0', marginTop: '0.3rem' }}>{formatDate(step.updatedAt)}</p>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </section>
        </div>
    );
};

export default OrderDetail;