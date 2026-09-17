import React, { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import axios from 'axios';
import { toast } from 'react-toastify';
import '../assets/order-history.css';

const API_BASE_URL = import.meta.env.VITE_API_CUSTOMER || import.meta.env.VITE_CUSTOMER_API_BASE_URL || "http://localhost:8082";

const PaymentResult = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const [status, setStatus] = useState('processing');
    const [orderNumber, setOrderNumber] = useState('');

    useEffect(() => {
        const verifyPayment = async () => {
            try {
                // Collect all query parameters to send to backend for verification
                const params = Object.fromEntries(searchParams.entries());
                
                const response = await axios.get(`${API_BASE_URL}/api/payment/vnpay/return`, { params });
                
                if (response.data.success) {
                    setStatus('success');
                    setOrderNumber(response.data.data);
                } else {
                    setStatus('failed');
                    setOrderNumber(response.data.data || '');
                }
            } catch (error) {
                console.error("Payment verification failed", error);
                setStatus('failed');
            }
        };

        verifyPayment();
    }, [searchParams]);

    return (
        <div className="payment-result-container" style={{ textAlign: 'center', padding: '50px', maxWidth: '600px', margin: '0 auto' }}>
            {status === 'processing' && (
                <div>
                    <i className="fas fa-spinner fa-spin fa-3x" style={{ color: '#0056b3' }}></i>
                    <h2>Đang xác thực thanh toán...</h2>
                    <p>Vui lòng không đóng trình duyệt.</p>
                </div>
            )}
            
            {status === 'success' && (
                <div>
                    <i className="fas fa-check-circle fa-4x" style={{ color: '#2e7d32', marginBottom: '20px' }}></i>
                    <h2 style={{ color: '#2e7d32' }}>Thanh toán thành công!</h2>
                    <p>Đơn hàng <strong>{orderNumber}</strong> của bạn đã được thanh toán.</p>
                    <button 
                        className="btn-primary" 
                        style={{ marginTop: '20px', padding: '10px 20px', borderRadius: '5px' }}
                        onClick={() => navigate('/history')}
                    >
                        Quay lại danh sách đơn hàng
                    </button>
                </div>
            )}

            {status === 'failed' && (
                <div>
                    <i className="fas fa-times-circle fa-4x" style={{ color: '#c62828', marginBottom: '20px' }}></i>
                    <h2 style={{ color: '#c62828' }}>Thanh toán thất bại</h2>
                    <p>Có lỗi xảy ra hoặc bạn đã hủy giao dịch đối với đơn hàng <strong>{orderNumber}</strong>.</p>
                    <button 
                        className="btn-primary" 
                        style={{ marginTop: '20px', padding: '10px 20px', borderRadius: '5px' }}
                        onClick={() => navigate('/history')}
                    >
                        Quay lại danh sách đơn hàng
                    </button>
                </div>
            )}
        </div>
    );
};

export default PaymentResult;
