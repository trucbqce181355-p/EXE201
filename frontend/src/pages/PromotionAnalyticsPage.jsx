import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { promotionAnalyticsApi } from '../services/api';
import {
    HiOutlineArrowLeft,
    HiOutlineArrowDownTray,
    HiOutlineArrowTrendingUp,
    HiOutlineTicket,
    HiOutlineCurrencyDollar,
    HiOutlineUsers
} from 'react-icons/hi2';
import {
    XAxis, YAxis, CartesianGrid,
    Tooltip, ResponsiveContainer, AreaChart, Area
} from 'recharts';

/**
 * Sub-component: Card hiển thị thông số tổng quan
 */
const AnalyticsCard = ({ label, value, icon, trend }) => (
    <div className="bg-white p-5 rounded-xl border border-[#f1e6dd] shadow-sm hover:shadow-md transition-shadow">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '8px' }}>
            <div style={{ padding: '8px', backgroundColor: '#fcfaf9', borderRadius: '8px', fontSize: '24px' }}>
                {icon}
            </div>
            <span style={{ fontSize: '12px', fontWeight: '500', color: '#16a34a', backgroundColor: '#f0fdf4', padding: '4px 8px', borderRadius: '9999px' }}>
                {trend}
            </span>
        </div>
        <p style={{ fontSize: '14px', color: '#6b7280', fontWeight: '500', margin: '4px 0' }}>{label}</p>
        <h4 style={{ fontSize: '24px', fontWeight: 'bold', color: '#5f3824', margin: 0 }}>{value}</h4>
    </div>
);

const PromotionAnalyticsPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();

    const [analytics, setAnalytics] = useState(null);
    const [usageData, setUsageData] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        if (id) {
            fetchData();
        }
    }, [id]);

    const fetchData = async () => {
        try {
            setLoading(true);
            setError(null);

            if (!promotionAnalyticsApi || !promotionAnalyticsApi.getAnalytics) {
                throw new Error("Promotion Analytics API service is not initialized correctly.");
            }

            const [details, history] = await Promise.all([
                promotionAnalyticsApi.getAnalytics(id),
                promotionAnalyticsApi.getUsageOverTime(id, 'day')
            ]);

            // CHI TIẾT SỬA: Map đúng các trường từ JSON API
            setAnalytics(details || {});

            // CHI TIẾT SỬA: Đảm bảo history là mảng để Recharts không bị lỗi
            setUsageData(Array.isArray(history) ? history : []);

        } catch (err) {
            console.error("Fetch Error:", err);
            setError(err.message || "Failed to load analytics data");
        } finally {
            setLoading(false);
        }
    };

    const handleExport = () => {
        try {
            promotionAnalyticsApi.exportData(id, 'excel');
        } catch (err) {
            alert("Export failed: " + err.message);
        }
    };

    if (loading) {
        return (
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '400px', width: '100%' }}>
                <div style={{ width: '40px', height: '40px', border: '4px solid #f1e6dd', borderTopColor: '#7b4a2e', borderRadius: '50%', animation: 'spin 1s linear infinite' }}></div>
                <style>{`@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }`}</style>
                <p style={{ marginTop: '16px', color: '#7b4a2e', fontWeight: '500' }}>Loading analytics...</p>
            </div>
        );
    }

    if (error) {
        return (
            <div style={{ padding: '40px', textAlign: 'center' }}>
                <div style={{ color: '#dc2626', marginBottom: '16px', fontWeight: 'bold' }}>⚠️ Error: {error}</div>
                <button
                    onClick={fetchData}
                    style={{ backgroundColor: '#7b4a2e', color: 'white', padding: '10px 24px', borderRadius: '8px', border: 'none', cursor: 'pointer' }}
                >
                    Try Again
                </button>
            </div>
        );
    }

    return (
        <div className="p-6" style={{ backgroundColor: '#fcfaf9', minHeight: '100vh' }}>
            {/* Header Section */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '32px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                    <button
                        onClick={() => navigate('/admin/promotions')}
                        style={{ padding: '8px', border: 'none', background: 'white', cursor: 'pointer', borderRadius: '50%', boxShadow: '0 2px 4px rgba(0,0,0,0.05)' }}
                    >
                        <HiOutlineArrowLeft size={24} color="#5f3824" />
                    </button>
                    <div>
                        <h2 style={{ fontSize: '24px', fontWeight: 'bold', color: '#452111', margin: 0 }}>Promotion Performance</h2>
                        <p style={{ fontSize: '14px', color: '#6b7280', margin: 0 }}>Campaign ID: #{id} | Data insights</p>
                    </div>
                </div>
                <button
                    onClick={handleExport}
                    style={{
                        display: 'flex', alignItems: 'center', gap: '8px',
                        backgroundColor: '#7b4a2e', color: 'white',
                        padding: '10px 20px', borderRadius: '8px',
                        border: 'none', cursor: 'pointer', fontWeight: '600'
                    }}
                >
                    <HiOutlineArrowDownTray size={20} /> Export Report
                </button>
            </div>

            {/* Top Cards Grid */}
            <div style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
                gap: '20px',
                marginBottom: '32px'
            }}>
                {/* SỬA: totalRedemptions -> totalUses */}
                <AnalyticsCard
                    label="Total Redemptions"
                    value={analytics?.totalUses?.toLocaleString() || 0}
                    icon={<HiOutlineTicket style={{ color: '#2563eb' }} />}
                    trend="+12%"
                />

                {/* SỬA: Nếu conversionRate là 0.2 (tức 20%), nhân 100 nếu cần. Giữ nguyên nếu API trả về 20 */}
                <AnalyticsCard
                    label="Conversion Rate"
                    value={`${(analytics?.conversionRate || 0).toFixed(1)}%`}
                    icon={<HiOutlineArrowTrendingUp style={{ color: '#16a34a' }} />}
                    trend="Stable"
                />

                {/* SỬA: totalRevenue -> totalRevenueGenerated */}
                <AnalyticsCard
                    label="Revenue Impact"
                    value={`$${(analytics?.totalRevenueGenerated || 0).toLocaleString()}`}
                    icon={<HiOutlineCurrencyDollar style={{ color: '#d97706' }} />}
                    trend="Gross"
                />

                <AnalyticsCard
                    label="Unique Customers"
                    value={analytics?.customerMetrics?.uniqueCustomers?.toLocaleString() || 0}
                    icon={<HiOutlineUsers style={{ color: '#7c3aed' }} />}
                    trend="Reach"
                />
            </div>

            {/* Main Chart Section - Thay thế bằng Bảng chi tiết chữ to */}
            <div style={{
                backgroundColor: 'white',
                padding: '32px',
                borderRadius: '24px',
                border: '1px solid #f1e6dd',
                marginBottom: '32px',
                boxShadow: '0 10px 15px -3px rgba(0,0,0,0.04)'
            }}>
                <h3 style={{
                    fontSize: '22px', // Tăng kích thước tiêu đề
                    fontWeight: 'bold',
                    color: '#452111',
                    marginBottom: '28px',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '10px'
                }}>
                    <span style={{ width: '4px', height: '24px', backgroundColor: '#7b4a2e', borderRadius: '4px' }}></span>
                    Usage History Details
                </h3>

                {usageData.length > 0 ? (
                    <div style={{ overflowX: 'auto' }}>
                        <table style={{ width: '100%', borderCollapse: 'separate', borderSpacing: '0 12px', textAlign: 'left' }}>
                            <thead>
                                <tr>
                                    <th style={{
                                        padding: '0 20px',
                                        color: '#8b6b55',
                                        fontWeight: '600',
                                        fontSize: '16px', // Chữ tiêu đề cột to hơn
                                        textTransform: 'uppercase',
                                        letterSpacing: '0.05em'
                                    }}>
                                        Date
                                    </th>
                                    <th style={{
                                        padding: '0 20px',
                                        color: '#8b6b55',
                                        fontWeight: '600',
                                        fontSize: '16px',
                                        textAlign: 'right',
                                        textTransform: 'uppercase',
                                        letterSpacing: '0.05em'
                                    }}>
                                        Redemptions
                                    </th>
                                </tr>
                            </thead>
                            <tbody>
                                {usageData.map((item, index) => (
                                    <tr key={index} style={{ transition: 'transform 0.2s' }}>
                                        <td style={{
                                            padding: '20px',
                                            backgroundColor: '#fcfaf9',
                                            borderRadius: '16px 0 0 16px',
                                            color: '#374151',
                                            fontSize: '18px', // Chữ nội dung to hơn
                                            fontWeight: '500'
                                        }}>
                                            {item.date}
                                        </td>
                                        <td style={{
                                            padding: '20px',
                                            backgroundColor: '#fcfaf9',
                                            borderRadius: '0 16px 16px 0',
                                            color: '#7b4a2e',
                                            fontSize: '20px', // Số lượng làm to và nổi bật hơn
                                            fontWeight: '800',
                                            textAlign: 'right'
                                        }}>
                                            {item.count || item.usageCount}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                ) : (
                    <div style={{
                        padding: '60px',
                        textAlign: 'center',
                        color: '#9ca3af',
                        border: '2px dashed #f1e6dd',
                        borderRadius: '20px',
                        fontSize: '18px'
                    }}>
                        No history data available for this campaign
                    </div>
                )}
            </div>

            {/* Bottom Section: Segments Performance */}
            {analytics?.topSegments && analytics.topSegments.length > 0 && (
                <div style={{ backgroundColor: 'white', padding: '24px', borderRadius: '16px', border: '1px solid #f1e6dd', boxShadow: '0 4px 6px -1px rgba(0,0,0,0.05)' }}>
                    <h3 style={{ fontSize: '18px', fontWeight: 'bold', color: '#5f3824', marginBottom: '20px' }}>Top Performing Segments</h3>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                        {analytics.topSegments.map((seg, i) => (
                            <div key={i} style={{ display: 'flex', justifyContent: 'space-between', padding: '14px 20px', backgroundColor: '#fcfaf9', borderRadius: '12px', border: '1px solid #f3f4f6' }}>
                                <span style={{ fontWeight: '600', color: '#374151' }}>{seg.name}</span>
                                <span style={{ color: '#7b4a2e', fontWeight: 'bold' }}>{seg.usageCount} redemptions</span>
                            </div>
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
};

export default PromotionAnalyticsPage;