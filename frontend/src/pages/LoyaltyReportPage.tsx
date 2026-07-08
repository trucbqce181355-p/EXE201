import React, { useEffect, useState } from 'react';
import { loyaltyReportApi } from '../services/api';
import { HiOutlineChartBar, HiOutlineUsers, HiOutlineTrophy, HiOutlineArrowTrendingUp, HiOutlineCalendar } from 'react-icons/hi2';
import { FiActivity } from 'react-icons/fi';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, BarChart, Bar, XAxis, YAxis, CartesianGrid } from 'recharts';

// Color palette synced with Admin brown theme
const COLORS = ['#7b4a2e', '#a67c52', '#d6b8a5', '#e8d8cc', '#5f3824'];

// --- 1. Interfaces synced with Backend DTOs ---
interface LoyaltyOverview {
    totalMembers: number;
    activeMembers: number;
    totalPointsIssued: number;
    totalPointsRedeemed: number;
    totalPointsExpired: number;
}

interface TierData {
    tierName: string;
    memberCount: number;
}

interface ActivityData {
    period: string;
    type: string;
    totalPoints: number;
}

interface EngagementData {
    avgPointsPerCustomer: number;
    redemptionRate: number;
}

const LoyaltyReportPage: React.FC = () => {
    const [overview, setOverview] = useState<LoyaltyOverview | null>(null);
    const [tiers, setTiers] = useState<TierData[]>([]);
    const [activities, setActivities] = useState<ActivityData[]>([]);
    const [engagement, setEngagement] = useState<EngagementData | null>(null);
    const [loading, setLoading] = useState<boolean>(true);

    useEffect(() => {
        fetchAllData();
    }, []);

    const fetchAllData = async () => {
        try {
            setLoading(true);
            const endDate = new Date().toISOString().split('T')[0];
            const startDate = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];

            const [ov, tr, ac, en] = await Promise.all([
                loyaltyReportApi.getOverview(),
                loyaltyReportApi.getTierDistribution(),
                loyaltyReportApi.getPointsActivity({ from: startDate, to: endDate }),
                loyaltyReportApi.getEngagement()
            ]);

            setOverview(ov);
            setTiers(tr || []);
            setActivities(ac || []);
            setEngagement(en);
        } catch (error) {
            console.error("Error fetching report data:", error);
        } finally {
            setLoading(false);
        }
    };

    if (loading || !overview) {
        return (
            <div className="admin-page" style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '400px' }}>
                <div className="flex flex-col items-center gap-2">
                    <div className="w-8 h-8 border-4 border-amber-900 border-t-transparent rounded-full animate-spin"></div>
                    <p className="text-amber-900 italic">Loading report data...</p>
                </div>
            </div>
        );
    }

    return (
        <div className="admin-page">
            <div className="page-header">
                <div>
                    <h2>Loyalty System Report</h2>
                    <p>Real-time analysis of Loyalty Balance & Point History</p>
                </div>
                <button onClick={fetchAllData} className="btn-primary" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <HiOutlineCalendar size={18} /> Refresh Data
                </button>
            </div>

            {/* 1. Overview Stat Cards */}
            <div className="report-grid">
                <StatCardAdmin
                    label="Total Members"
                    value={overview.totalMembers.toLocaleString()}
                    icon={<HiOutlineUsers size={28} color="#7b4a2e" />}
                />
                <StatCardAdmin
                    label="Points Issued"
                    value={overview.totalPointsIssued.toLocaleString()}
                    icon={<HiOutlineTrophy size={28} color="#7b4a2e" />}
                />
                <StatCardAdmin
                    label="Points Redeemed"
                    value={overview.totalPointsRedeemed.toLocaleString()}
                    icon={<FiActivity size={28} color="#7b4a2e" />}
                />
                <StatCardAdmin
                    label="Redemption Rate"
                    value={`${((engagement?.redemptionRate || 0) * 100).toFixed(1)}%`}
                    icon={<HiOutlineArrowTrendingUp size={28} color="#7b4a2e" />}
                />
            </div>

            {/* 2. Charts Section */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(450px, 1fr))', gap: '20px', marginBottom: '24px' }}>

                {/* Pie Chart: Tier Distribution */}
                <div className="chart-container-admin" style={{ background: '#fff', padding: '20px', borderRadius: '12px', border: '1px solid #f1e6dd' }}>
                    <h3 style={{ color: '#5f3824', fontSize: '16px', marginBottom: '16px', fontWeight: 600 }}>Membership Tier Structure</h3>
                    <div style={{ height: '260px' }}>
                        {tiers.length > 0 ? (
                            <ResponsiveContainer width="100%" height="100%">
                                <PieChart>
                                    <Pie data={tiers} dataKey="memberCount" nameKey="tierName" cx="50%" cy="50%" outerRadius={80} label>
                                        {tiers.map((_, i) => <Cell key={`cell-${i}`} fill={COLORS[i % COLORS.length]} />)}
                                    </Pie>
                                    <Tooltip />
                                </PieChart>
                            </ResponsiveContainer>
                        ) : <p className="text-center pt-20 text-gray-400">No tier data available</p>}
                    </div>
                </div>

                {/* Bar Chart: Points Activity */}
                <div className="chart-container-admin" style={{ background: '#fff', padding: '20px', borderRadius: '12px', border: '1px solid #f1e6dd' }}>
                    <h3 style={{ color: '#5f3824', fontSize: '16px', marginBottom: '16px', fontWeight: 600 }}>Daily Points Fluctuations</h3>
                    <div style={{ height: '260px' }}>
                        {activities.length > 0 ? (
                            <ResponsiveContainer width="100%" height="100%">
                                <BarChart data={activities}>
                                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1e6dd" />
                                    <XAxis dataKey="period" fontSize={11} tickFormatter={(v) => v.split('-').slice(1).join('/')} />
                                    <YAxis fontSize={11} />
                                    <Tooltip />
                                    <Bar dataKey="totalPoints" fill="#7b4a2e" radius={[4, 4, 0, 0]} />
                                </BarChart>
                            </ResponsiveContainer>
                        ) : <p className="text-center pt-20 text-gray-400">No activity data available</p>}
                    </div>
                </div>
            </div>

            {/* 3. Detailed Data Table */}
            <div className="admin-table-container">
                <table className="admin-table">
                    <thead>
                        <tr>
                            <th>Period</th>
                            <th>Activity Type</th>
                            <th style={{ textAlign: 'right' }}>Points Amount</th>
                        </tr>
                    </thead>
                    <tbody>
                        {activities.length > 0 ? (
                            activities.map((act, idx) => (
                                <tr key={idx}>
                                    <td>{act.period}</td>
                                    <td><strong>{act.type}</strong></td>
                                    <td style={{ textAlign: 'right', fontWeight: 'bold', color: act.totalPoints >= 0 ? '#2e7d32' : '#7a1f1f' }}>
                                        {act.totalPoints > 0 ? `+${act.totalPoints.toLocaleString()}` : act.totalPoints.toLocaleString()}
                                    </td>
                                </tr>
                            ))
                        ) : (
                            <tr><td colSpan={3} style={{ textAlign: 'center', padding: '20px' }}>No transactions found</td></tr>
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

// Internal Stat Card Component
const StatCardAdmin: React.FC<{ label: string; value: string; icon: React.ReactNode }> = ({ label, value, icon }) => (
    <div className="stat-card-admin" style={{ background: '#fff', padding: '20px', borderRadius: '12px', border: '1px solid #f1e6dd' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between' }}>
            <div>
                <p style={{ fontSize: '12px', color: '#8b6b55', fontWeight: 600, textTransform: 'uppercase' }}>{label}</p>
                <h3 style={{ fontSize: '24px', color: '#5f3824', margin: '4px 0' }}>{value}</h3>
            </div>
            <div style={{ background: '#fcfaf9', padding: '10px', borderRadius: '8px' }}>{icon}</div>
        </div>
    </div>
);

export default LoyaltyReportPage;