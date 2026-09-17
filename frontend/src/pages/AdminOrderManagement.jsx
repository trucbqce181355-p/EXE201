import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { toast } from 'react-toastify';

const API_BASE_URL = import.meta.env.VITE_API_CUSTOMER || import.meta.env.VITE_CUSTOMER_API_BASE_URL || "http://localhost:8082";

const AdminOrderManagement = () => {
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(1);
    const [totalPages, setTotalPages] = useState(1);

    const fetchOrders = async (pageNum = 1) => {
        setLoading(true);
        const token = localStorage.getItem('accessToken');
        try {
            const response = await axios.get(`${API_BASE_URL}/admin/orders?page=${pageNum}&limit=10`, {
                headers: { Authorization: `Bearer ${token}` }
            });
            console.log("Admin orders response:", response.data);
            const data = response.data.data;
            setOrders(data.data || data.content || data || []);
            setTotalPages(Math.ceil((data.total || data.totalElements || 0) / 10));
            setPage(pageNum);
        } catch (error) {
            console.error("Error fetching admin orders:", error);
            toast.error("Không thể tải danh sách đơn hàng.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchOrders();
    }, []);

    const handleUpdateStatus = async (orderId, newStatus) => {
        const token = localStorage.getItem('accessToken');
        try {
            await axios.put(`${API_BASE_URL}/admin/orders/${orderId}/status`, { newStatus, note: 'Admin updated' }, {
                headers: { Authorization: `Bearer ${token}` }
            });
            toast.success(`Cập nhật đơn hàng ${orderId} thành ${newStatus}`);
            fetchOrders(page);
        } catch (error) {
            console.error("Error updating status:", error);
            toast.error(error.response?.data?.message || "Lỗi cập nhật trạng thái");
        }
    };

    const formatCurrency = (amount) => {
        return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
    };

    const formatDate = (dateString) => {
        if (!dateString) return 'N/A';
        const date = new Date(dateString);
        return date.toLocaleDateString('vi-VN') + ' ' + date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
    };

    return (
        <div className="admin-page-container">
            <div className="admin-page-header">
                <h1 className="admin-page-title">Quản lý Đơn hàng</h1>
                <p className="admin-page-description">Xem và cập nhật trạng thái toàn bộ đơn hàng</p>
            </div>

            <div className="admin-table-card">
                <div className="admin-table-container">
                    {loading ? (
                        <div style={{ padding: '20px', textAlign: 'center' }}>
                            <i className="fas fa-spinner fa-spin fa-2x"></i>
                        </div>
                    ) : (
                        <table className="admin-table">
                            <thead>
                                <tr>
                                    <th>Mã Đơn</th>
                                    <th>Khách hàng</th>
                                    <th>Ngày đặt</th>
                                    <th>Tổng tiền</th>
                                    <th>Thanh toán</th>
                                    <th>Trạng thái</th>
                                    <th>Hành động</th>
                                </tr>
                            </thead>
                            <tbody>
                                {orders.length > 0 ? orders.map((order) => (
                                    <tr key={order.orderId}>
                                        <td style={{ fontWeight: 'bold' }}>{order.orderNumber || `ORD-${order.orderId}`}</td>
                                        <td>{order.customerName}</td>
                                        <td>{formatDate(order.createdAt)}</td>
                                        <td>{formatCurrency(order.totalAmount)}</td>
                                        <td>
                                            <span className={`status-badge ${order.paymentStatus === 'PAID' ? 'active' : 'inactive'}`}>
                                                {order.paymentStatus}
                                            </span>
                                        </td>
                                        <td>
                                            <span style={{
                                                padding: '4px 8px', borderRadius: '4px', fontSize: '12px', fontWeight: 'bold',
                                                background: order.status === 'Completed' ? '#e8f5e9' : (order.status === 'Cancelled' ? '#ffebee' : '#fff3e0'),
                                                color: order.status === 'Completed' ? '#2e7d32' : (order.status === 'Cancelled' ? '#c62828' : '#ef6c00')
                                            }}>
                                                {order.status}
                                            </span>
                                        </td>
                                        <td>
                                            {order.status !== 'Completed' && order.status !== 'Cancelled' && (
                                                <select 
                                                    style={{ padding: '5px', borderRadius: '4px', border: '1px solid #ddd' }}
                                                    onChange={(e) => {
                                                        if (e.target.value) handleUpdateStatus(order.orderId, e.target.value);
                                                        e.target.value = ""; // Reset
                                                    }}
                                                    defaultValue=""
                                                >
                                                    <option value="" disabled>Đổi trạng thái</option>
                                                    <option value="Processing">Processing</option>
                                                    <option value="Shipped">Shipped</option>
                                                    <option value="Completed">Completed</option>
                                                    <option value="Cancelled">Cancelled</option>
                                                </select>
                                            )}
                                        </td>
                                    </tr>
                                )) : (
                                    <tr>
                                        <td colSpan="7" style={{ textAlign: 'center', padding: '20px' }}>Không có đơn hàng nào</td>
                                    </tr>
                                )}
                            </tbody>
                        </table>
                    )}
                </div>

                <div className="admin-pagination">
                    <button 
                        className="admin-pagination-btn" 
                        disabled={page === 1} 
                        onClick={() => fetchOrders(page - 1)}
                    >
                        <i className="fas fa-chevron-left"></i>
                    </button>
                    <span className="admin-pagination-info">Trang {page} / {totalPages}</span>
                    <button 
                        className="admin-pagination-btn" 
                        disabled={page >= totalPages} 
                        onClick={() => fetchOrders(page + 1)}
                    >
                        <i className="fas fa-chevron-right"></i>
                    </button>
                </div>
            </div>
        </div>
    );
};

export default AdminOrderManagement;
