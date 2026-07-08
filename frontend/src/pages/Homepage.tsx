import React, { useEffect, useRef, useState } from 'react';
import '../assets/style.css';
import axios from 'axios';
import '@fortawesome/fontawesome-free/css/all.min.css';
import { useNavigate, Link } from 'react-router-dom';
import { dispatchSessionChanged } from '../lib/apiClient';
import { customerApi, productApi, cartApi, blogApi, contactApi } from '../services/api';
import Login from "./Login.tsx";
import PromotionPublicSection from '../components/promotions/PromotionPublicSection';
import Swal from 'sweetalert2';

type ActivePanel = 'login' | 'update' | 'forgot' | null;

type MessageState = {
    type: 'success' | 'error';
    text: string;
} | null;

export type LoginResponse = {
    accessToken?: string;
    refreshToken?: string;
    userId?: number;
};

type ApiResponse<T> = {
    success: boolean;
    message: string;
    data: T;
};

type AccountProfile = {
    id?: number;
    username?: string;
    email: string;
    fullName: string;
    phone: string;
    avatarUrl: string;
    status?: string;
    roles?: string[];
    updatedAt?: string;
};

const API_BASE_URL = 'http://localhost:8081';
const CUS_API_BASE_URL = 'http://localhost:8082';
/** Google Maps (or other) embed URL; leave empty to skip iframe (avoids React warning on src=""). */
const CONTACT_MAP_EMBED_URL = 'https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3929.0533542569997!2d105.72985667479377!3d10.012451790093568!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x31a0882139720a77%3A0x3916a227d0b95a64!2zVHLGsOG7nW5nIMSQ4bqhaSBo4buNYyBGUFQgQ-G6p24gVGjGoQ!5e0!3m2!1sen!2s!4v1783436328625!5m2!1sen!2s';
const createEmptyProfile = (email = ''): AccountProfile => ({
    email,
    fullName: '',
    phone: '',
    avatarUrl: ''
});

/** Backend often sends null for optional strings; inputs must use "" not null (controlled components). */
const normalizeProfileForState = (
    profile: Partial<AccountProfile>,
    fallbackEmail = ''
): AccountProfile => {
    const email = profile.email || fallbackEmail || '';
    return {
        ...createEmptyProfile(email),
        ...profile,
        email,
        fullName: profile.fullName ?? '',
        phone: profile.phone ?? '',
        avatarUrl: profile.avatarUrl ?? ''
    };
};

const getErrorMessage = (error: unknown) => {
    if (axios.isAxiosError(error)) {
        const responseData = error.response?.data;

        if (typeof responseData === 'string' && responseData.trim()) {
            return responseData;
        }

        if (responseData?.error) {
            return responseData.error;
        }

        if (responseData?.message) {
            return responseData.message;
        }

        if (responseData?.detail) {
            return responseData.detail;
        }

        return error.message || 'Request failed.';
    }

    if (error instanceof Error) {
        return error.message;
    }

    return 'Unexpected error.';
};

const getDisplayName = (profile: AccountProfile, email?: string | null) => {
    const fullName = profile.fullName?.trim();
    if (fullName) {
        return fullName;
    }

    const username = profile.username?.trim();
    if (username) {
        return username;
    }

    if (email) {
        return email.split('@')[0];
    }

    if (profile.email) {
        return profile.email.split('@')[0];
    }

    return 'Account';
};

const getInitials = (value: string) => {
    const parts = value.trim().split(/\s+/).filter(Boolean).slice(0, 2);
    if (parts.length === 0) {
        return 'A';
    }

    return parts.map(part => part[0].toUpperCase()).join('');
};

const hasRole = (roles: string[] | undefined, ...expectedRoles: string[]) =>
    Array.isArray(roles) && roles.some(role => expectedRoles.includes(role));

const isAuthFailure = (error: unknown) =>
    axios.isAxiosError(error) && (error.response?.status === 401 || error.response?.status === 403);

const isValidEmail = (value: string) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
const isValidPhone = (value: string) => /^\+?[0-9]{9,15}$/.test(value);

const isValidHttpUrl = (value: string) => {
    try {
        const parsed = new URL(value);
        return parsed.protocol === 'http:' || parsed.protocol === 'https:';
    } catch {
        return false;
    }
};

const Homepage: React.FC = () => {
    const navigate = useNavigate();
    const [activePanel, setActivePanel] = useState<ActivePanel>(null);
    const [loggedInUser, setLoggedInUser] = useState<string | null>(null);
    const [isUserMenuOpen, setIsUserMenuOpen] = useState(false);
    const [isNavbarOpen, setIsNavbarOpen] = useState(false);
    const userMenuRef = useRef<HTMLDivElement | null>(null);

    const [loginData, setLoginData] = useState({
        email: '',
        password: ''
    });
    const [updateData, setUpdateData] = useState<AccountProfile>(createEmptyProfile());
    const [forgotEmail, setForgotEmail] = useState('');
    const [forgotFieldError, setForgotFieldError] = useState('');
    const [loginMessage, setLoginMessage] = useState<MessageState>(null);
    const [updateMessage, setUpdateMessage] = useState<MessageState>(null);
    const [forgotMessage, setForgotMessage] = useState<MessageState>(null);
    const [appNotice, setAppNotice] = useState<MessageState>(null);
    const [isLoggingIn, setIsLoggingIn] = useState(false);
    const [isUpdatingAccount, setIsUpdatingAccount] = useState(false);
    const [isRequestingReset, setIsRequestingReset] = useState(false);
    const [isHomepageAvatarBroken, setIsHomepageAvatarBroken] = useState(false);

    const [contactName, setContactName] = useState('');
    const [contactEmail, setContactEmail] = useState('');
    const [contactPhone, setContactPhone] = useState('');
    const [contactMessage, setContactMessage] = useState('');
    const [isSendingContact, setIsSendingContact] = useState(false);
    const [newsletterEmail, setNewsletterEmail] = useState('');

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
            images: [{ id: 1, imageUrl: "images/menu-1.png", isPrimary: true }]
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
            images: [{ id: 2, imageUrl: "images/menu-2.png", isPrimary: true }]
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
            images: [{ id: 3, imageUrl: "images/menu-3.png", isPrimary: true }]
        }
    ];

    const [homepageProducts, setHomepageProducts] = useState<any[]>([]);
    const [selectedProductDetail, setSelectedProductDetail] = useState<any>(null);
    const [isLoadingDetail, setIsLoadingDetail] = useState(false);
    const [addToCartLoading, setAddToCartLoading] = useState<number | null>(null);
    const [blogPosts, setBlogPosts] = useState<any[]>([]);

    useEffect(() => {
        const fetchHomepageProducts = async () => {
            try {
                const res = await productApi.getAll();
                const list = res?.data?.data || res?.data || [];
                if (list.length > 0) {
                    setHomepageProducts(list);
                } else {
                    setHomepageProducts(fallbackProducts);
                }
            } catch (err) {
                console.error("Lỗi khi tải sản phẩm trang chủ:", err);
                setHomepageProducts(fallbackProducts);
            }
        };

        const fetchBlogPosts = async () => {
            try {
                const res = await blogApi.getAll();
                const list = res?.data || res || [];
                setBlogPosts(list);
            } catch (err) {
                console.error("Lỗi khi tải bài viết trang chủ:", err);
            }
        };

        fetchHomepageProducts();
        fetchBlogPosts();
    }, []);

    useEffect(() => {
        const handleHashChange = () => {
            const hash = window.location.hash;
            if (hash) {
                const id = hash.substring(1);
                const element = document.getElementById(id);
                if (element) {
                    setTimeout(() => {
                        element.scrollIntoView({ behavior: 'smooth' });
                    }, 150);
                }
            }
        };

        handleHashChange();
        window.addEventListener('hashchange', handleHashChange);
        return () => window.removeEventListener('hashchange', handleHashChange);
    }, []);

    const handleLogoClick = (e: React.MouseEvent) => {
        if (window.location.pathname === '/' || window.location.pathname === '') {
            e.preventDefault();
            const homeSection = document.getElementById('home');
            if (homeSection) {
                homeSection.scrollIntoView({ behavior: 'smooth' });
            } else {
                window.scrollTo({ top: 0, behavior: 'smooth' });
            }
            window.history.pushState(null, '', '#home');
        } else {
            navigate('/#home');
        }
    };

    const handleViewBlogPostDetail = (post: any) => {
        const urlStr = post.content ? post.content.trim() : "";
        if (urlStr.startsWith("http://") || urlStr.startsWith("https://")) {
            window.open(urlStr, '_blank', 'noopener,noreferrer');
        } else {
            Swal.fire({
                title: post.title,
                html: `
                    <div style="text-align: left; font-size: 1.5rem; line-height: 1.8; color: #444; max-height: 60vh; overflow-y: auto; padding-right: 0.5rem;">
                        <div style="text-align: center; margin-bottom: 2rem;">
                            <img src="${post.imageUrl || 'images/g-img-1.jpg'}" style="max-width: 100%; max-height: 350px; object-fit: cover; border-radius: 1rem; box-shadow: 0 4px 10px rgba(0,0,0,0.1);" />
                        </div>
                        <div style="margin-bottom: 1.5rem; font-size: 1.4rem; color: #888; display: flex; justify-content: space-between; border-bottom: 1px solid #eee; padding-bottom: 1rem;">
                            <span><i class="fas fa-user"></i> Tác giả: <strong>${post.author || 'Admin'}</strong></span>
                            <span><i class="fas fa-calendar"></i> Ngày đăng: <strong>${post.publishedAt ? new Date(post.publishedAt).toLocaleDateString('vi-VN') : new Date().toLocaleDateString('vi-VN')}</strong></span>
                        </div>
                        <div style="white-space: pre-wrap; text-align: justify;">${post.content}</div>
                    </div>
                `,
                width: '800px',
                confirmButtonText: 'Đóng',
                confirmButtonColor: '#512a10'
            });
        }
    };

    const handleViewProductDetail = (productId: number) => {
        navigate(`/products/${productId}`);
    };

    const handleAddToCart = async (product: any) => {
        if (!loggedInUser) {
            showAppNotice('error', 'Vui lòng đăng nhập để thêm vào giỏ hàng!');
            openLoginPanel();
            return;
        }
        setAddToCartLoading(product.id);
        try {
            await cartApi.addItem(product.id, product.name, product.price, 1);
            showAppNotice('success', `Đã thêm ${product.name} vào giỏ hàng!`);
        } catch (err) {
            showAppNotice('error', 'Không thể thêm sản phẩm vào giỏ hàng.');
        } finally {
            setAddToCartLoading(null);
        }
    };

    const showAppNotice = (type: 'success' | 'error', text: string) => {
        setAppNotice({ type, text });
    };

    const clearClientSession = () => {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('email');
        localStorage.removeItem('accountProfile');
        localStorage.removeItem('username');
        setLoggedInUser(null);
        setUpdateData(createEmptyProfile());
        setForgotEmail('');
        setIsUserMenuOpen(false);
    };

    const persistProfile = (profile: AccountProfile, fallbackEmail?: string | null) => {
        const normalizedProfile = normalizeProfileForState(profile, fallbackEmail || '');

        localStorage.setItem('accountProfile', JSON.stringify(normalizedProfile));

        if (normalizedProfile.email) {
            localStorage.setItem('email', normalizedProfile.email);
        }

        setUpdateData(normalizedProfile);
        setLoggedInUser(normalizedProfile.email || null);
        setForgotEmail(normalizedProfile.email || '');

        return normalizedProfile;
    };

    const refreshAccessToken = async () => {
        const refreshToken = localStorage.getItem('refreshToken');
        if (!refreshToken) {
            throw new Error('Session expired. Please log in again.');
        }

        const response = await axios.post<LoginResponse>(`${API_BASE_URL}/api/auth/refresh`, {
            refreshToken
        });

        const nextAccessToken = response.data.accessToken;
        if (!nextAccessToken) {
            throw new Error('Unable to refresh session. Please log in again.');
        }

        localStorage.setItem('accessToken', nextAccessToken);

        if (response.data.refreshToken) {
            localStorage.setItem('refreshToken', response.data.refreshToken);
        }

        return nextAccessToken;
    };

    const fetchAccountProfile = async (token: string) =>
        axios.get<ApiResponse<AccountProfile>>(`${API_BASE_URL}/api/accounts/me`, {
            headers: {
                Authorization: `Bearer ${token}`
            }
        });

    const syncProfileWithServer = async (tokenOverride?: string, fallbackEmail?: string | null) => {
        let token = tokenOverride || localStorage.getItem('accessToken');
        if (!token) {
            return null;
        }

        try {
            const response = await fetchAccountProfile(token);
            return persistProfile(response.data.data, fallbackEmail);
        } catch (error) {
            if (!isAuthFailure(error)) {
                throw error;
            }

            token = await refreshAccessToken();
            const response = await fetchAccountProfile(token);
            return persistProfile(response.data.data, fallbackEmail);
        }
    };


    useEffect(() => {
        const token = localStorage.getItem('accessToken');
        const savedEmail = localStorage.getItem('email');
        const savedProfile = localStorage.getItem('accountProfile');

        if (savedEmail) {
            setLoggedInUser(savedEmail);
            setLoginData(prev => ({ ...prev, email: savedEmail }));
            setForgotEmail(savedEmail);
            setUpdateData(createEmptyProfile(savedEmail));
        }

        if (!savedProfile) {
            return;
        }

        try {
            const parsedProfile = JSON.parse(savedProfile) as AccountProfile;
            const email = parsedProfile.email || savedEmail || '';

            setUpdateData(normalizeProfileForState(parsedProfile, email));
        } catch (restoreError) {
            console.error('Unable to restore account profile from localStorage.', restoreError);
        }

        if (!token) {
            return;
        }

        syncProfileWithServer(token, savedEmail).catch((error) => {
            const errorMessage = getErrorMessage(error);
            if (isAuthFailure(error) || /refresh session|log in again|expired token/i.test(errorMessage)) {
                clearClientSession();
                return;
            }

            console.error('Unable to sync account profile from backend.', error);
        });
    }, []);

    useEffect(() => {
        setIsHomepageAvatarBroken(false);
    }, [updateData.avatarUrl]);

    useEffect(() => {
        if (!appNotice) {
            return;
        }

        const timeoutId = window.setTimeout(() => {
            setAppNotice(null);
        }, 2800);

        return () => window.clearTimeout(timeoutId);
    }, [appNotice]);

    useEffect(() => {
        if (!isUserMenuOpen) {
            return;
        }

        const handleClickOutside = (event: MouseEvent) => {
            if (userMenuRef.current && !userMenuRef.current.contains(event.target as Node)) {
                setIsUserMenuOpen(false);
            }
        };

        const handleEscape = (event: KeyboardEvent) => {
            if (event.key === 'Escape') {
                setIsUserMenuOpen(false);
            }
        };

        document.addEventListener('mousedown', handleClickOutside);
        document.addEventListener('keydown', handleEscape);

        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
            document.removeEventListener('keydown', handleEscape);
        };
    }, [isUserMenuOpen]);

    const closePanels = () => {
        setActivePanel(null);
        setLoginMessage(null);
        setUpdateMessage(null);
        setForgotMessage(null);
        setIsUserMenuOpen(false);
    };

    const openLoginPanel = () => {
        setLoginMessage(null);
        setActivePanel('login');
    };

    const openUpdatePanel = () => {
        if (!loggedInUser) {
            return;
        }

        setIsUserMenuOpen(false);
        navigate('/account');
    };

    const openCustomerProfile = async () => {
        const userId = updateData.id;

        if (!loggedInUser || !userId) {
            showAppNotice('error', 'Customer profile is unavailable right now.');
            return;
        }

        setIsUserMenuOpen(false);

        try {
            const response = await customerApi.getCustomerByUserId(userId);
            const customerId = response.data?.data?.customerId || response.data?.customerId;

            if (!customerId) {
                throw new Error('Customer profile not found.');
            }

            navigate(`/customers/${customerId}`);
        } catch (error) {
            showAppNotice('error', getErrorMessage(error));
        }
    };

    const openViewOrderHistory = () => {
        if (!loggedInUser) {
            return;
        }

        setIsUserMenuOpen(false);
        navigate('/order-history');
    }

    const toggleUserMenu = () => {
        setIsUserMenuOpen(prev => !prev);
    };

    const openForgotPanel = () => {
        const email = loggedInUser || loginData.email || forgotEmail;

        setForgotMessage(null);
        setForgotFieldError('');
        setForgotEmail(email);
        setActivePanel('forgot');
    };

    const displayName = getDisplayName(updateData, loggedInUser);
    const avatarInitials = getInitials(displayName);
    const isCustomerAccount = hasRole(updateData.roles, 'ROLE_CUSTOMER', 'CUSTOMER');
    const canAccessAdminPanel = hasRole(
        updateData.roles,
        'ROLE_ADMIN',
        'MANAGER',
        'ROLE_MANAGER',
        'ADMIN'
    );

    const handleLoginChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setLoginData(prev => ({ ...prev, [name]: value }));
    };

    const handleUpdateChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setUpdateData(prev => ({ ...prev, [name]: value }));
    };

    const handleForgotEmailChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { value } = e.target;
        setForgotEmail(value);
        setForgotFieldError('');

        if (forgotMessage?.type === 'error') {
            setForgotMessage(null);
        }
    };

    const handleLogin = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoginMessage(null);

        const email = loginData.email.trim();
        const password = loginData.password.trim();

        if (!email || !password) {
            setLoginMessage({
                type: 'error',
                text: 'Enter email and password.'
            });
            return;
        }

        if (!isValidEmail(email)) {
            setLoginMessage({
                type: 'error',
                text: 'Enter a valid email.'
            });
            return;
        }

        setIsLoggingIn(true);

        try {
            const response = await axios.post<LoginResponse>(`${API_BASE_URL}/api/auth/login`, loginData);

            if (response.status === 200) {

                const { accessToken } = response.data;

                if (!accessToken) {
                    throw new Error('Login response did not contain an access token.');
                }

                localStorage.setItem('accessToken', accessToken);
                localStorage.setItem('email', loginData.email);

                console.log(accessToken)
                persistProfile(createEmptyProfile(loginData.email), loginData.email);


                if (response.data.refreshToken) {
                    localStorage.setItem('refreshToken', response.data.refreshToken);
                }
                localStorage.removeItem('username');

                setLoggedInUser(loginData.email); // Cập nhật giao diện
                try {

                    const profile = await syncProfileWithServer(accessToken, loginData.email);

                    //check role để redirect
                    console.log("profile");
                    if (profile) {
                        // Cập nhật State của Homepage ngay tại chỗ để giao diện đổi luôn
                        setUpdateData(profile);
                        setLoggedInUser(profile.email);

                        console.log(profile.roles);
                        // 3. ĐIỀU HƯỚNG (Redirect)
                        // Nếu đại ca muốn trải nghiệm mượt mà (SPA), hãy dùng navigate
                        // Nếu muốn sạch sẽ tuyệt đối, hãy dùng window.location.href
                        if (profile.roles?.includes('CUSTOMER')) {
                            navigate('/');
                        } else {
                            navigate('/admin');
                        }
                    }
                } catch (syncError) {
                    console.error('Unable to sync profile after login.', syncError);
                }
                setLoginData(prev => ({ ...prev, password: '' }));
                closePanels();
                localStorage.removeItem('username');
                showAppNotice('success', 'Signed in.');

                try {
                    const savedProfile = localStorage.getItem('accountProfile');
                    console.log(savedProfile)
                    let userId = null;

                    if (savedProfile) {
                        const profile = JSON.parse(savedProfile);
                        console.log("Toàn bộ profile nè đại ca:", profile);
                        userId = profile.id;
                    }

                    const token = localStorage.getItem('accessToken');
                    if (!token) return;
                    const payload = JSON.parse(atob(token.split(".")[1]));
                    const customerResp = await customerApi.getByUserId(payload.userId);
                    // Lưu lại dùng cho trang Order History

                    const customerId = customerResp?.id || customerResp?.data?.id;
                    console.log("customerID: " + customerId);
                    localStorage.setItem('customerId', customerId);
                } catch (e) {
                    console.log("Không tìm thấy thông tin Customer tương ứng với User này");
                }
                console.log('Homepage: Login successful, dispatching session changed event');
                dispatchSessionChanged();
            }
        } catch (error) {
            setLoginMessage({
                type: 'error',
                text: getErrorMessage(error)
            });
        } finally {
            setIsLoggingIn(false);
        }
    };

    const handleUpdateAccount = async (e: React.FormEvent) => {
        e.preventDefault();
        setUpdateMessage(null);

        const fullName = updateData.fullName.trim();
        const phone = updateData.phone.trim();
        const avatarUrl = updateData.avatarUrl.trim();

        if (!fullName) {
            setUpdateMessage({
                type: 'error',
                text: 'Enter your name.'
            });
            return;
        }

        if (!phone) {
            setUpdateMessage({
                type: 'error',
                text: 'Enter your phone.'
            });
            return;
        }

        if (!isValidPhone(phone)) {
            setUpdateMessage({
                type: 'error',
                text: 'Enter a valid phone number.'
            });
            return;
        }

        if (!avatarUrl) {
            setUpdateMessage({
                type: 'error',
                text: 'Enter an avatar URL.'
            });
            return;
        }

        if (!isValidHttpUrl(avatarUrl)) {
            setUpdateMessage({
                type: 'error',
                text: 'Enter a valid image URL.'
            });
            return;
        }

        setIsUpdatingAccount(true);

        const token = localStorage.getItem('accessToken');
        if (!token) {
            setUpdateMessage({
                type: 'error',
                text: 'Please log in before updating your account.'
            });
            setIsUpdatingAccount(false);
            return;
        }

        try {
            const response = await axios.put<ApiResponse<AccountProfile>>(
                `${API_BASE_URL}/api/accounts/me`,
                {
                    fullName: updateData.fullName,
                    phone: updateData.phone,
                    avatarUrl: updateData.avatarUrl
                },
                {
                    headers: {
                        Authorization: `Bearer ${token}`
                    }
                }
            );

            const returnedProfile = response.data.data;
            const normalizedProfile = persistProfile(
                returnedProfile,
                returnedProfile.email || loggedInUser || updateData.email
            );
            setLoggedInUser(normalizedProfile.email);
            setUpdateMessage({
                type: 'success',
                text: response.data.message || 'Account updated successfully.'
            });
        } catch (error) {
            setUpdateMessage({
                type: 'error',
                text: getErrorMessage(error)
            });
        } finally {
            setIsUpdatingAccount(false);
        }
    };

    const handleForgotPasswordRequest = async (e: React.FormEvent) => {
        e.preventDefault();
        setForgotMessage(null);
        setForgotFieldError('');

        const email = forgotEmail.trim();
        if (!email) {
            setForgotFieldError('Enter your email.');
            return;
        }

        if (!isValidEmail(email)) {
            setForgotFieldError('Enter a valid email.');
            return;
        }

        setIsRequestingReset(true);

        try {
            const response = await axios.post<ApiResponse<null>>(
                `${API_BASE_URL}/api/accounts/forgot-password/request`,
                { email }
            );

            setForgotMessage({
                type: 'success',
                text: response.data.message || 'If the email exists, a reset link has been sent.'
            });
        } catch (error) {
            setForgotMessage({
                type: 'error',
                text: getErrorMessage(error)
            });
        } finally {
            setIsRequestingReset(false);
        }
    };

    const handleContactSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsSendingContact(true);
        try {
            await contactApi.submitContact({
                name: contactName,
                email: contactEmail,
                phone: contactPhone,
                message: contactMessage
            });
            Swal.fire({
                icon: 'success',
                title: 'Thành công!',
                text: 'Cảm ơn bạn đã gửi liên hệ. Chúng tôi sẽ phản hồi lại bạn sớm nhất!',
                confirmButtonColor: '#3e704d'
            });
            setContactName('');
            setContactEmail('');
            setContactPhone('');
            setContactMessage('');
        } catch (error) {
            Swal.fire({
                icon: 'error',
                title: 'Lỗi!',
                text: getErrorMessage(error) || 'Gửi liên hệ thất bại. Vui lòng thử lại sau.',
                confirmButtonColor: '#3e704d'
            });
        } finally {
            setIsSendingContact(false);
        }
    };

    const handleNewsletterSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (newsletterEmail.trim()) {
            Swal.fire({
                icon: 'success',
                title: 'Đăng ký thành công!',
                text: 'Cảm ơn bạn đã đăng ký nhận bản tin từ BeAn. Chúng tôi sẽ gửi những thông tin khuyến mãi và cập nhật mới nhất qua email của bạn!',
                confirmButtonColor: '#3e704d'
            });
            setNewsletterEmail('');
        }
    };

    const handleLogout = async () => {
        setIsUserMenuOpen(false);
        // 1. Lấy token từ localStorage ra để gửi đi
        const token = localStorage.getItem('accessToken');

        if (token) {
            try {
                // 2. Gửi request POST lên BE để blacklist token này

                await axios.post(`${API_BASE_URL}/api/auth/logout`, {}, {
                    headers: {
                        'Authorization': `Bearer ${token}` // Gửi token theo định dạng Bearer
                    }
                });
            } catch (error) {
                console.error("Lỗi khi gọi API logout ở BE:", error);

            }
        }

        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('email');
        localStorage.removeItem('accountProfile');
        localStorage.removeItem('username');
        localStorage.removeItem('customerId');
        setLoggedInUser(null);
        setUpdateData(createEmptyProfile());
        setLoginData(prev => ({ ...prev, password: '' }));
        setForgotEmail('');
        closePanels();

        showAppNotice('success', 'Signed out.');
        dispatchSessionChanged();
    };

    const handleCartClick = () => {
        if (!loggedInUser) {
            // Nếu chưa đăng nhập, thông báo và mở panel login
            showAppNotice('error', 'Vui lòng đăng nhập để xem giỏ hàng!');
            openLoginPanel();
        } else {
            // Nếu đã đăng nhập, điều hướng sang trang cart
            navigate('/cart');
        }
    };
    return (
        <>
            <div className={`app-toast ${appNotice ? `show ${appNotice.type}` : ''}`}>
                {appNotice && (
                    <>
                        <i className={`fas ${appNotice.type === 'success' ? 'fa-circle-check' : 'fa-circle-exclamation'}`}></i>
                        <span>{appNotice.text}</span>
                    </>
                )}
            </div>

            <header className="header fixed-top">
                <div className="container d-flex align-items-center w-100">

                    <a href="/#home" onClick={handleLogoClick} className="logo d-flex align-items-center" style={{ textDecoration: "none" }}>
                        <img src="/images/logo.png" alt="BeAn Logo" style={{ height: "50px", width: "50px", borderRadius: "50%", objectFit: "cover" }} />
                    </a>

                    <nav className={`navbar d-flex align-items-center ms-auto ${isNavbarOpen ? 'active' : ''}`}>
                        <a href="#home" onClick={() => setIsNavbarOpen(false)}>Trang chủ</a>
                        {/* <a href="#promotions" onClick={() => setIsNavbarOpen(false)}>Khuyến mãi</a> */}
                        <a href="#about" onClick={() => setIsNavbarOpen(false)}>Giới thiệu</a>
                        <a href="#menu" onClick={() => setIsNavbarOpen(false)}>Sản phẩm</a>
                        <a href="#reviews" onClick={() => setIsNavbarOpen(false)}>Đánh giá</a>
                        <a href="#blogs" onClick={() => setIsNavbarOpen(false)}>Tin tức</a>
                        <a href="#contact" onClick={() => setIsNavbarOpen(false)}>Liên hệ</a>
                    </nav>
                    <div className="icons d-flex align-items-center ms-4">
                        <div id="menu-btn" className="fas fa-bars" onClick={() => setIsNavbarOpen(!isNavbarOpen)}></div>
                        <div
                            className="fas fa-shopping-cart"
                            onClick={handleCartClick}
                            style={{ cursor: 'pointer', marginRight: '1.5rem' }}
                        ></div>
                        {loggedInUser ? (
                            <div
                                className={`user-menu-container ${isUserMenuOpen ? 'open' : ''}`}
                                ref={userMenuRef}
                            >
                                <button
                                    type="button"
                                    className="user-profile-trigger"
                                    onClick={toggleUserMenu}
                                    aria-haspopup="menu"
                                    aria-expanded={isUserMenuOpen}
                                >
                                    {updateData.avatarUrl && !isHomepageAvatarBroken ? (
                                        <img
                                            src={updateData.avatarUrl}
                                            alt={displayName}
                                            className="user-avatar"
                                            onLoad={() => setIsHomepageAvatarBroken(false)}
                                            onError={() => setIsHomepageAvatarBroken(true)}
                                        />
                                    ) : (
                                        <span className="user-avatar-placeholder">{avatarInitials}</span>
                                    )}
                                    <span className="user-profile-text">
                                        <strong>{displayName}</strong>
                                        
                                    </span>
                                    <i className="fas fa-chevron-down user-profile-caret"></i>
                                </button>
                                <div className="user-dropdown" role="menu">

                                    {canAccessAdminPanel && (
                                        <button type="button" onClick={() => navigate('/admin')}>
                                            Quản trị hệ thống
                                        </button>
                                    )}

                                    {isCustomerAccount ? (
                                        <>
                                            <button type="button" onClick={openCustomerProfile}>
                                                Hồ sơ cá nhân
                                            </button>
                                            <button type="button" onClick={openViewOrderHistory}>
                                                Lịch sử đơn hàng
                                            </button>
                                        </>
                                    ) : (
                                        <button type="button" onClick={openUpdatePanel}>
                                            Xem hồ sơ
                                        </button>
                                    )}

                                    <button type="button" onClick={() => navigate('/loyalty')}>
                                        Khách hàng thân thiết
                                    </button>

                                    <button type="button" onClick={handleLogout}>
                                        Đăng xuất
                                    </button>

                                </div>
                            </div>
                        ) : (
                            <div id="login-btn" className="fas fa-user" onClick={openLoginPanel}></div>
                        )}
                    </div>
                </div>
            </header>



            {/* login form ends */}
            <Login
                isActive={activePanel === 'login'}
                onClose={closePanels}
                onLoginSuccess={(email, accessToken, refreshToken) => {
                    localStorage.setItem('accessToken', accessToken);
                    if (refreshToken) {
                        localStorage.setItem('refreshToken', refreshToken);
                    }
                    localStorage.setItem('email', email);
                    setLoggedInUser(email);
                    closePanels();
                }}
                openForgotPanel={openForgotPanel}
                getErrorMessage={getErrorMessage}
                isValidEmail={isValidEmail}
                API_BASE_URL={API_BASE_URL}
            />
            <div className={`login-form-container ${activePanel === 'update' ? 'active' : ''}`}>
                <div id="close-login-btn" className="fas fa-times" onClick={closePanels}></div>

                <form onSubmit={handleUpdateAccount} noValidate>
                    <h3>Cập nhật tài khoản</h3>

                    {updateMessage && (
                        <p className={`auth-message ${updateMessage.type}`}>{updateMessage.text}</p>
                    )}

                    <input
                        type="email"
                        value={updateData.email ?? ''}
                        className="box auth-readonly-box"
                        readOnly
                    />

                    <input
                        type="text"
                        name="fullName"
                        placeholder="Họ và tên của bạn"
                        className="box"
                        value={updateData.fullName ?? ''}
                        onChange={handleUpdateChange}
                    />

                    <input
                        type="tel"
                        name="phone"
                        placeholder="Số điện thoại của bạn"
                        className="box"
                        value={updateData.phone ?? ''}
                        onChange={handleUpdateChange}
                    />

                    <input
                        type="url"
                        name="avatarUrl"
                        placeholder="Đường dẫn ảnh đại diện"
                        className="box"
                        value={updateData.avatarUrl ?? ''}
                        onChange={handleUpdateChange}
                    />

                    {updateData.avatarUrl && (
                        <img
                            src={updateData.avatarUrl}
                            alt="Avatar preview"
                            className="auth-avatar-preview"
                            onError={(event) => {
                                event.currentTarget.style.display = 'none';
                            }}
                            onLoad={(event) => {
                                event.currentTarget.style.display = 'block';
                            }}
                        />
                    )}

                    <button type="submit" className="link-btn" disabled={isUpdatingAccount}>
                        {isUpdatingAccount ? 'Đang lưu...' : 'Lưu thay đổi'}
                    </button>
                </form>
            </div>
            <div className={`login-form-container ${activePanel === 'forgot' ? 'active' : ''}`}>
                <form className="auth-login-card auth-forgot-card" onSubmit={handleForgotPasswordRequest} noValidate>
                    <button
                        type="button"
                        className="auth-close-btn"
                        onClick={closePanels}
                        aria-label="Close forgot password"
                    >
                        <i className="fas fa-times"></i>
                    </button>

                    <div className="auth-login-top">
                        <span className="auth-login-badge">Khôi phục</span>
                        <h3>Quên mật khẩu</h3>
                        <p>Chúng tôi sẽ gửi liên kết đặt lại mật khẩu cho bạn.</p>
                    </div>

                    {forgotMessage && (
                        <p className={`auth-message ${forgotMessage.type}`}>{forgotMessage.text}</p>
                    )}

                    <div className="auth-login-fields">
                        <label
                            className={`auth-input-shell ${forgotFieldError ? 'has-error' : ''}`}
                            htmlFor="forgot-email"
                        >
                            <i className="fas fa-envelope"></i>
                            <input
                                id="forgot-email"
                                type="email"
                                placeholder="Email"
                                className="box"
                                value={forgotEmail}
                                onChange={handleForgotEmailChange}
                                aria-invalid={Boolean(forgotFieldError)}
                                aria-describedby={forgotFieldError ? 'forgot-email-error' : undefined}
                            />
                        </label>
                        {forgotFieldError && (
                            <p id="forgot-email-error" className="field-error-text auth-field-error">
                                {forgotFieldError}
                            </p>
                        )}
                    </div>

                    <button type="submit" className="link-btn auth-login-submit" disabled={isRequestingReset}>
                        <i className={`fas ${isRequestingReset ? 'fa-spinner fa-spin' : 'fa-paper-plane'}`}></i>
                        <span>{isRequestingReset ? 'Đang gửi...' : 'Gửi liên kết khôi phục'}</span>
                    </button>

                    <button type="button" className="auth-text-btn auth-login-link" onClick={openLoginPanel}>
                        Quay lại đăng nhập
                    </button>
                </form>
            </div>
            {/* header section ends    */}

            {/* home section starts  */}

            <section className="home" id="home">

                <div className="container">

                    <div className="row align-items-center text-center text-md-left min-vh-100">
                        <div className="col-md-8 col-lg-7">
                            <h3>BeAn - Be Every Angle</h3>
                            <p className="slogan">Bảo vệ người thân yêu trong mọi chuyến đi</p>
                            <a href="#menu" className="link-btn">Xem ngay</a>
                        </div>
                    </div>

                </div>

            </section>

            {/* home section ends */}

            {/* <PromotionPublicSection /> */}

            {/* about section starts  */}

            <section className="about" id="about">

                <div className="container">

                    <div className="row align-items-center">
                        <div className="col-md-6">
                            <img src="images/about-img-1.png" className="w-100" alt="" />
                        </div>
                        <div className="col-md-6">
                            <span>why choose us?</span>
                            <h3 className="title">BeAn - Be Every Angle</h3>
                            <p>BeAn là giải pháp thông minh giúp phát hiện người còn trong xe và gửi cảnh báo tức thì. Chúng tôi cam kết mang đến sự an toàn, tiện lợi và yên tâm cho mọi gia đình thông qua công nghệ hiện đại và dễ sử dụng.</p>
                            <a href="#" className="link-btn">Xem thêm</a>
                            <div className="icons-container">
                                <div className="icons">
                                    <i className="fas fa-brain"></i>
                                    <h3>Phát hiện thông minh</h3>
                                </div>
                                <div className="icons">
                                    <i className="fas fa-bell"></i>
                                    <h3>Cảnh báo tức thì</h3>
                                </div>
                                <div className="icons">
                                    <i className="fas fa-tools"></i>
                                    <h3>Dễ dàng lắp đặt</h3>
                                </div>
                            </div>
                        </div>
                    </div>

                </div>

            </section>

            {/* about section ends */}

            {/* menu section starts  */}

            <section className="menu" id="menu">

                <h1 className="heading"> Sản phẩm của chúng tôi </h1>

                <div className="container box-container">
                    {homepageProducts.length > 0 ? (
                        homepageProducts.map((p, index) => {
                            const placeholders = [
                                "images/menu-1.png",
                                "images/menu-2.png",
                                "images/menu-3.png",
                                "images/menu-4.png",
                                "images/menu-5.png",
                                "images/menu-6.png"
                            ];
                            const defaultPlaceholder = placeholders[(p.id || index) % placeholders.length];
                            const imageUrl = p.thumbnailUrl || defaultPlaceholder;
                            return (
                                <div className="box" key={p.id}>
                                    <img src={imageUrl} alt={p.name} style={{ width: '100%', height: '260px', objectFit: 'cover', borderRadius: '8px' }} />
                                    {p.tags && p.tags.length > 0 && (
                                        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '5px', justifyContent: 'center', marginTop: '10px' }}>
                                            {p.tags.map((tag, idx) => (
                                                <span key={idx} style={{
                                                    backgroundColor: '#eefeeb',
                                                    color: '#2e7d32',
                                                    border: '1px solid #2e7d32',
                                                    padding: '2px 8px',
                                                    borderRadius: '4px',
                                                    fontSize: '1rem',
                                                    fontWeight: 'bold'
                                                }}>{tag}</span>
                                            ))}
                                        </div>
                                    )}
                                    <h3>{p.name}</h3>
                                    <p style={{ minHeight: '60px' }}>{p.description || "Thiết bị bảo vệ chất lượng cao dành cho hành trình của bạn."}</p>
                                    <div style={{ fontSize: '18px', fontWeight: 'bold', color: '#5d4037', margin: '10px 0' }}>
                                        {p.price?.toLocaleString()} đ
                                    </div>
                                    <div style={{ display: 'flex', gap: '10px', justifyContent: 'center' }}>
                                        <button
                                            type="button"
                                            className="link-btn"
                                            style={{ margin: 0, padding: '10px 15px' }}
                                            onClick={() => handleViewProductDetail(p.id)}
                                        >
                                            Xem chi tiết
                                        </button>
                                        <button
                                            type="button"
                                            className="link-btn"
                                            style={{ margin: 0, padding: '10px 15px', backgroundColor: '#5d4037', color: 'white' }}
                                            onClick={() => handleAddToCart(p)}
                                            disabled={addToCartLoading === p.id}
                                        >
                                            {addToCartLoading === p.id ? '...' : <i className="fas fa-shopping-cart"></i>}
                                        </button>
                                    </div>
                                </div>
                            );
                        })
                    ) : (
                        <>
                            <div className="box">
                                <img src="images/menu-1.png" alt="" />
                                <h3>Cảm biến BeAn</h3>
                                <p>Cảm biến thông minh phát hiện chuyển động trong xe.</p>
                                <a href="#menu" className="link-btn">Xem thêm</a>
                            </div>
                            <div className="box">
                                <img src="images/menu-2.png" alt="" />
                                <h3>Bộ điều khiển BeAn</h3>
                                <p>Điều khiển trung tâm kết nối các thiết bị cảm biến.</p>
                                <a href="#menu" className="link-btn">Xem thêm</a>
                            </div>
                            <div className="box">
                                <img src="images/menu-3.png" alt="" />
                                <h3>Còi cảnh báo BeAn</h3>
                                <p>Còi hú âm lượng lớn cảnh báo tức thì khi có sự cố.</p>
                                <a href="#menu" className="link-btn">Xem thêm</a>
                            </div>
                        </>
                    )}
                </div>

            </section>

            {/* menu section ends */}



            {/* reviews section starts  */}

            <section className="reviews" id="reviews">

                <h1 className="heading">reviews</h1>

                <div className="box-container container">

                    <div className="box">
                        <img src="images/pic-1.png" alt="" />
                        <h3>Dennis Nziokiss</h3>
                        <p>Lorem, ipsum dolor sit amet consectetur adipisicing elit. Suscipit, ipsum eos? Perspiciatis expedita laudantium blanditiis cupiditate at natus, quam alias?</p>
                        <div className="stars">
                            <i className="fas fa-star"></i>
                            <i className="fas fa-star"></i>
                            <i className="fas fa-star"></i>
                            <i className="fas fa-star"></i>
                            <i className="fas fa-star-half-alt"></i>
                        </div>
                    </div>

                    <div className="box">
                        <img src="images/pic-2.png" alt="" />
                        <h3>Dennis Nzioki</h3>
                        <p>Lorem, ipsum dolor sit amet consectetur adipisicing elit. Suscipit, ipsum eos? Perspiciatis expedita laudantium blanditiis cupiditate at natus, quam alias?</p>
                        <div className="stars">
                            <i className="fas fa-star"></i>
                            <i className="fas fa-star"></i>
                            <i className="fas fa-star"></i>
                            <i className="fas fa-star"></i>
                            <i className="fas fa-star-half-alt"></i>
                        </div>
                    </div>

                    <div className="box">
                        <img src="images/pic-3.png" alt="" />
                        <h3>Dennis Nzioki</h3>
                        <p>Lorem, ipsum dolor sit amet consectetur adipisicing elit. Suscipit, ipsum eos? Perspiciatis expedita laudantium blanditiis cupiditate at natus, quam alias?</p>
                        <div className="stars">
                            <i className="fas fa-star"></i>
                            <i className="fas fa-star"></i>
                            <i className="fas fa-star"></i>
                            <i className="fas fa-star"></i>
                            <i className="fas fa-star-half-alt"></i>
                        </div>
                    </div>

                </div>

            </section>

            {/* reviews section ends */}

            {/* blogs section starts  */}

            <section className="blogs" id="blogs">

                <h1 className="heading"> Tin Tức </h1>

                <div className="box-container container">
                    {blogPosts && blogPosts.length > 0 ? (
                        blogPosts.map((post: any) => (
                            <div className="box" key={post.id}>
                                <div className="image">
                                    <img src={post.imageUrl || "images/g-img-1.jpg"} alt={post.title} />
                                </div>
                                <div className="content">
                                    <h3>{post.title}</h3>

                                    <button
                                        type="button"
                                        className="blog-btn"
                                        onClick={() => handleViewBlogPostDetail(post)}

                                    >
                                        Xem thêm
                                    </button>
                                </div>
                                <div className="icons">
                                    <span> <i className="fas fa-calendar"></i> {post.publishedAt ? new Date(post.publishedAt).toLocaleDateString('vi-VN') : '21st may, 2023'} </span>
                                    <span> <i className="fas fa-user"></i> by {post.author || 'admin'} </span>
                                </div>
                            </div>
                        ))
                    ) : (
                        <div style={{ width: "100%", textAlign: "center", padding: "4rem 0", fontSize: "1.8rem", color: "#666", gridColumn: "1 / -1" }}>
                            <i className="far fa-newspaper" style={{ fontSize: "4.8rem", marginBottom: "1.5rem", color: "#ccc" }}></i>
                            <p>Hiện tại chưa có bài viết nào được đăng.</p>
                        </div>
                    )}
                </div>

            </section>

            {/* blogs section ends */}

            {/* contact section starts  */}

            <section className="contact" id="contact">

                <h1 className="heading"> Liên Hệ  </h1>

                <div className="container">

                    <div className="contact-info-container">

                        <div className="box">
                            <i className="fas fa-phone"></i>
                            <h3>Điện thoại</h3>
                            <p>+84 352880023</p>
                        </div>

                        <div className="box">
                            <i className="fas fa-envelope"></i>
                            <h3>Email</h3>
                            <p>bean@gmail.com</p>
                        </div>

                        <div className="box">
                            <i className="fas fa-map"></i>
                            <h3>Địa chỉ</h3>
                            <p>600 Nguyễn Văn Cừ Nối Dài, An Bình, Cần Thơ</p>
                        </div>

                    </div>

                    <div className="row align-items-center">

                        <div className="col-md-6 mb-5 mb-md-0 ">
                            {CONTACT_MAP_EMBED_URL ? (
                                <iframe
                                    className="map w-100"
                                    src={CONTACT_MAP_EMBED_URL}
                                    width={600}
                                    height={450}
                                    style={{ border: 0 }}
                                    allowFullScreen={true}
                                    loading="lazy"
                                    title="Map"
                                />
                            ) : (
                                <div
                                    className="map w-100 bg-light"
                                    style={{ minHeight: 450, border: 0 }}
                                    role="presentation"
                                    aria-hidden
                                />
                            )}
                        </div>

                        <form onSubmit={handleContactSubmit} className="col-md-6">
                            <h3>Phản Hồi</h3>
                            <input
                                type="text"
                                placeholder="Tên của bạn"
                                className="box"
                                value={contactName}
                                onChange={(e) => setContactName(e.target.value)}
                                required
                            />
                            <input
                                type="email"
                                placeholder="Email"
                                className="box"
                                value={contactEmail}
                                onChange={(e) => setContactEmail(e.target.value)}
                                required
                            />
                            <input
                                type="text"
                                placeholder="Điện thoại"
                                className="box"
                                value={contactPhone}
                                onChange={(e) => setContactPhone(e.target.value)}
                                required
                            />
                            <textarea
                                name="message"
                                placeholder="Lời Nhắn"
                                className="box"
                                cols={30}
                                rows={10}
                                value={contactMessage}
                                onChange={(e) => setContactMessage(e.target.value)}
                                required
                            ></textarea>
                            <input
                                type="submit"
                                value={isSendingContact ? "Đang gửi..." : "Gửi"}
                                className="link-btn"
                                disabled={isSendingContact}
                            />
                        </form>

                    </div>
                </div>


            </section >

            {/* contact section ends */}

            {/* newsletter section starts  */}

            <section className="newsletter">
                <div className="container">
                    <h3>Đăng ký bản tin</h3>
                    <p>Nhận các thông tin khuyến mãi và cập nhật mới nhất từ BeAn</p>
                    <form onSubmit={handleNewsletterSubmit}>
                        <input
                            type="email"
                            name="email"
                            placeholder="Nhập email của bạn"
                            className="email"
                            value={newsletterEmail}
                            onChange={(e) => setNewsletterEmail(e.target.value)}
                            required
                        />
                        <input type="submit" value="Đăng ký" className="link-btn" />
                    </form>
                </div>
            </section>

            {/* newsletter section ends */}

            {/* footer section starts  */}

            <section className="footer container">

                <a href="/#home" onClick={handleLogoClick} className="logo d-flex align-items-center justify-content-center" style={{ textDecoration: "none", marginBottom: "1.5rem" }}>
                    <img src="/images/logo.png" alt="BeAn Logo" style={{ height: "45px", width: "45px", borderRadius: "50%", objectFit: "cover" }} />
                </a>

                <p className="credit"> phát triển bởi <span>GR6_EXE101_G14</span> </p>

                <div className="share">
                    <a href="https://www.facebook.com/share/195KwfVGre/?mibextid=wwXIfr" target="_blank" rel="noopener noreferrer" className="fab fa-facebook-f"></a>
                    <a href="https://www.instagram.com/" target="_blank" rel="noopener noreferrer" className="fab fa-instagram"></a>
                    <a href="https://www.tiktok.com/" target="_blank" rel="noopener noreferrer" className="fab fa-tiktok"></a>
                </div>

            </section>

            {/* footer section ends */}



        </>
    );
};

export default Homepage;
