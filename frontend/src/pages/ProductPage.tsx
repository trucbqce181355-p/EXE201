import { useEffect, useState } from "react";
import { productApi } from "../services/api";
import { Product } from "../types/product";
import { useNavigate } from "react-router-dom";
import { toast } from "react-toastify";
import "../assets/product.css";
import '@fortawesome/fontawesome-free/css/all.min.css';
import ProductImageManager from "../components/ProductImageManager";

const ProductPage = () => {
    const [products, setProducts] = useState<Product[]>([]);
    const [categories, setCategories] = useState<any[]>([]);
    const [search, setSearch] = useState<string>("");
    const [filterStatus, setFilterStatus] = useState<string>("");
    const [filterAvailable, setFilterAvailable] = useState<string>("");
    const [activeProduct, setActiveProduct] = useState(null);
    const [searchResults, setSearchResults] = useState([]);

    // UI Modals States
    const [showModal, setShowModal] = useState(false);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [showTagModal, setShowTagModal] = useState(false);
    const [deleteId, setDeleteId] = useState<number | null>(null);
    const [successPopup, setSuccessPopup] = useState({ show: false, message: "" });

    // Mode State (Create vs Update)
    const [isEditMode, setIsEditMode] = useState(false);
    const [currentProductId, setCurrentProductId] = useState<number | null>(null);

    // Tag management state
    const [tags, setTags] = useState<any[]>([]);
    const [productTags, setProductTags] = useState<any[]>([]);
    const [selectedTags, setSelectedTags] = useState<string[]>([]);

    // Bulk category assignment state
    const [bulkCategories, setBulkCategories] = useState<any[]>([]);
    const [selectedProducts, setSelectedProducts] = useState<number[]>([]);
    const [selectedCategory, setSelectedCategory] = useState<string>("");

    const navigate = useNavigate();
    const [errors, setErrors] = useState<any>({});
    const [form, setForm] = useState({
        name: "", sku: "", price: "", category_id: "",
        description: "", is_available: true, preparation_time: ""
    });

    useEffect(() => {
        fetchProducts();
        fetchCategories();
        fetchBulkCategories();
        fetchTags();
    }, []);

    const fetchProducts = async () => {
        try {
            const res = await productApi.getAll();
            setProducts(res?.data?.data || []);
        } catch (err) {
            toast.error("Failed to load products.");
        }
    };

    const fetchCategories = async () => {
        try {
            const res = await productApi.getAllCate();
            setCategories(res?.data?.data || res?.data || []);
        } catch (err) {
            console.error("Fetch categories error:", err);
        }
    };

    const fetchBulkCategories = async () => {
        try {
            const res = await productApi.getAllCategoriesForDropdown();
            setBulkCategories(res?.data || []);
        } catch (err) {
            toast.error("Failed to load categories for bulk assignment.");
        }
    };

    const fetchTags = async () => {
        try {
            const res = await productApi.getAllTags();
            setTags(res?.data || []);
        } catch (err) {
            console.error("Failed to load tags:", err);
        }
    };

    const handleRowClick = (productId: number) => {
        navigate(`/admin/products/${productId}`);
    };

    // --- FORM MODAL LOGIC ---
    const openCreateModal = () => {
        setIsEditMode(false);
        setCurrentProductId(null);
        setSelectedTags([]);
        setForm({ name: "", sku: "", price: "", category_id: "", description: "", is_available: true, preparation_time: "" });
        setErrors({});
        setShowModal(true);
    };

    const openEditModal = async (e: React.MouseEvent, product: any) => {
        e.stopPropagation();
        setIsEditMode(true);
        setCurrentProductId(product.id);
        setSelectedTags([]);

        try {
            const productTagsRes = await productApi.getProductTags(product.id);
            setSelectedTags(productTagsRes?.data?.map((tag: any) => String(tag.id)) || []);
        } catch (err) {
            console.error("Failed to load product tags:", err);
        }

        // Tìm ID của Category dựa trên Name để gán vào select box
        const cate = categories.find(c => c.name === product.categoryName);

        setForm({
            name: product.name,
            sku: product.sku,
            price: String(product.price),
            category_id: cate ? String(cate.id) : "",
            description: product.description || "",
            is_available: product.available,
            preparation_time: product.preparationTime ? String(product.preparationTime) : ""
        });
        setErrors({});
        setShowModal(true);
    };

    const openImageProduct = (e: React.MouseEvent, product: any) => {
        e.stopPropagation();
        setActiveProduct(product);    
    };

    const handleChange = (e: any) => {
        const { name, value, type, checked } = e.target;
        setForm({ ...form, [name]: type === "checkbox" ? checked : value });
        if (errors[name]) {
            setErrors((prev: any) => {
                const newErrors = { ...prev };
                delete newErrors[name];
                return newErrors;
            });
        }
    };

    const validateForm = () => {
        const newErrors: any = {};
        if (!form.name.trim()) newErrors.name = "Name is required";
        if (!form.sku.trim()) newErrors.sku = "SKU is required";
        if (!form.price) newErrors.price = "Price is required";
        if (!form.category_id) newErrors.category_id = "Select a category";
        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const showSuccess = (msg: string) => {
        setSuccessPopup({ show: true, message: msg });
        setTimeout(() => setSuccessPopup({ show: false, message: "" }), 1500);
    };

    // --- SUBMIT LOGIC (CREATE & UPDATE) ---
    const handleSubmit = async () => {
        if (!validateForm()) return;
        try {
            const payload = {
                name: form.name,
                sku: form.sku,
                price: Number(form.price),
                categoryId: Number(form.category_id),
                description: form.description,
                available: form.is_available,
                preparationTime: form.preparation_time ? Number(form.preparation_time) : null
            };

            if (isEditMode && currentProductId) {
                await productApi.update(currentProductId, payload);
                await productApi.assignTags(currentProductId, { tagIds: selectedTags });
                showSuccess("Product Updated Successfully! 🔄");
            } else {
                const res = await productApi.create(payload);
                const newProductId = res?.data?.id;
                if (newProductId) {
                    await productApi.assignTags(newProductId, { tagIds: selectedTags });
                }
                showSuccess("Product Created Successfully! ✨");
            }

            setShowModal(false);
            fetchProducts();
        } catch (err: any) {
            const backendMessage = err.response?.data?.message;
            if (backendMessage === "SKU already exists") {
                setErrors((prev: any) => ({ ...prev, sku: "This SKU is already taken!" }));
            } else {
                toast.error("Action failed. Please try again.");
            }
        }
    };

    // --- DELETE LOGIC ---
    const openDeleteModal = (e: React.MouseEvent, id: number) => {
        e.stopPropagation();
        setDeleteId(id);
        setShowDeleteModal(true);
    };

    const handleDelete = async () => {
        if (!deleteId) return;
        try {
            await productApi.delete(Number(deleteId));
            setShowDeleteModal(false);
            showSuccess("Product Deleted Successfully! 🗑️");
            fetchProducts();
            setDeleteId(null);
        } catch (err) {
            toast.error("Delete failed!");
        }
    };

    // --- TAG MANAGEMENT LOGIC ---
    const openTagModal = async (e: React.MouseEvent, product: any) => {
        e.stopPropagation();
        setCurrentProductId(product.id);
        setSelectedTags([]);

        try {
            // Fetch available tags and current product tags
            const [tagsRes, productTagsRes] = await Promise.all([
                productApi.getAllTags(),
                productApi.getProductTags(product.id)
            ]);

            setTags(tagsRes?.data || []);
            setProductTags(productTagsRes?.data || []);
            // Set selected tags from product's current tags
            setSelectedTags(productTagsRes?.data?.map((tag: any) => String(tag.id)) || []);
        } catch (err) {
            toast.error("Failed to load tags data.");
        }

        setShowTagModal(true);
    };

    const handleTagSelection = (tagId: string) => {
        // For checkboxes, multiple tags can be selected
        setSelectedTags(prev =>
            prev.includes(tagId)
                ? prev.filter(id => id !== tagId)
                : [...prev, tagId]
        );
    };
    const handleAssignTags = async () => {
        if (!currentProductId) return;

        try {
            await productApi.assignTags(currentProductId, { tagIds: selectedTags });

            // Refresh product data
            const productRes = await productApi.getById(currentProductId);
            setProductTags(productRes?.data?.tags || []);

            showSuccess("Tags assigned successfully! 🏷️");
            fetchProducts();
            setShowTagModal(false);
        } catch (err) {
            toast.error("Failed to assign tags.");
        }
    };
    // --- BULK CATEGORY ASSIGNMENT LOGIC ---
    const handleProductSelection = (productId: number) => {
        setSelectedProducts(prev =>
            prev.includes(productId)
                ? prev.filter(id => id !== productId)
                : [...prev, productId]
        );
    };

    const handleSelectAll = () => {
        if (selectedProducts.length === filteredProducts.length) {
            setSelectedProducts([]);
        } else {
            setSelectedProducts(filteredProducts.map(p => p.id));
        }
    };

    const handleBulkAssignCategory = async () => {
        if (!selectedCategory || selectedProducts.length === 0) {
            toast.error("Please select category and at least one product.");
            return;
        }

        try {
            await productApi.bulkAssignCategory({
                productIds: selectedProducts,
                categoryId: Number(selectedCategory)
            });

            // Refresh products
            fetchProducts();

            // Reset selection
            setSelectedProducts([]);
            setSelectedCategory("");

            showSuccess("Bulk category assignment successful! 📂");
        } catch (err) {
            toast.error("Failed to assign category to products.");
        }
    };

    const filteredProducts = products.filter((p) => {
        return (
            (!search || p.name.toLowerCase().includes(search.toLowerCase())) &&
            (!filterStatus || p.status === filterStatus) &&
            (!filterAvailable || String(p.available) === filterAvailable)
        );
    });

    return (
        <div className="product-page">
            <div className="product-header">
                <h2>Product Management</h2>
                <button className="create-btn" onClick={openCreateModal}>
                    <i className="fas fa-plus"></i> Create Product
                </button>
            </div>

            {/* Bulk Category Assignment Section */}
            <div className="bulk-assignment-section" style={{
                background: '#fff',
                padding: '20px',
                borderRadius: '10px',
                marginBottom: '20px',
                boxShadow: '0 2px 8px rgba(0,0,0,0.08)',
                display: 'flex',
                alignItems: 'center',
                gap: '15px'
            }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                    <input
                        type="checkbox"
                        checked={selectedProducts.length === filteredProducts.length && filteredProducts.length > 0}
                        onChange={handleSelectAll}
                        style={{ width: '18px', height: '18px' }}
                    />
                    <span style={{ fontWeight: '500' }}>Select All ({selectedProducts.length} selected)</span>
                </div>

                <select
                    value={selectedCategory}
                    onChange={(e) => setSelectedCategory(e.target.value)}
                    style={{
                        padding: '8px 12px',
                        borderRadius: '6px',
                        border: '1px solid #ccc',
                        minWidth: '200px'
                    }}
                >
                    <option value="">Select Category</option>
                    {bulkCategories.map((cat) => (
                        <option key={cat.id} value={cat.id}>{cat.name}</option>
                    ))}
                </select>

                <button
                    onClick={handleBulkAssignCategory}
                    disabled={!selectedCategory || selectedProducts.length === 0}
                    style={{
                        padding: '8px 16px',
                        borderRadius: '6px',
                        backgroundColor: selectedCategory && selectedProducts.length > 0 ? '#28a745' : '#6c757d',
                        color: 'white',
                        border: 'none',
                        cursor: selectedCategory && selectedProducts.length > 0 ? 'pointer' : 'not-allowed',
                        fontWeight: '500'
                    }}
                >
                    Assign Category to Selected
                </button>
            </div>

            <div className="product-filters">
                <input placeholder="Search product..." value={search} onChange={(e) => setSearch(e.target.value)} />
                <select value={filterStatus} onChange={(e) => setFilterStatus(e.target.value)}>
                    <option value="">All Status</option>
                    <option value="ACTIVE">ACTIVE</option>
                    <option value="INACTIVE">INACTIVE</option>
                </select>
                <select value={filterAvailable} onChange={(e) => setFilterAvailable(e.target.value)}>
                    <option value="">All Availability</option>
                    <option value="true">Available</option>
                    <option value="false">Off</option>
                </select>
            </div>

            <div className="product-table">
                <div className="table-header-row">
                    <span className="col-checkbox">
                        {/* <input
                            type="checkbox"
                            checked={selectedProducts.length === filteredProducts.length && filteredProducts.length > 0}
                            onChange={handleSelectAll}
                            style={{ width: '18px', height: '18px' }}
                        /> */}
                    </span>
                    <span className="col-info" style={{ marginRight: "-20px" }}>Product Info</span>
                    <span className="col-price" style={{ marginRight: "-20px" }}>Price</span>
                    <span className="col-price">Cate</span>
                    <span className="col-status"></span>
                    <span className="col-stock"></span>
                    <span className="col-actions">Actions</span>
                </div>

                {filteredProducts.map((p) => (
                    <div key={p.id} className="product-row" onClick={() => handleRowClick(p.id)}>
                        <div className="col-checkbox" onClick={(e) => e.stopPropagation()} style={{ marginRight: "20px" }}>
                            <input
                                type="checkbox"
                                checked={selectedProducts.includes(p.id)}
                                onChange={() => handleProductSelection(p.id)}
                                style={{ width: '18px', height: '18px' }}
                            />
                        </div>
                        <div className="col-info">
                            <h3>{p.name}</h3>
                            <div className="sub-info" style={{ display: 'flex', flexWrap: 'wrap', gap: '5px', alignItems: 'center' }}>
                                <span className="sku-tag">#{p.sku}</span>
                                <span className="cate-tag">{p.categoryName}</span>
                                {p.tags && p.tags.map((t: string, idx: number) => (
                                    <span key={idx} style={{
                                        backgroundColor: '#512a10',
                                        color: '#fff',
                                        padding: '2px 8px',
                                        borderRadius: '4px',
                                        fontSize: '1.1rem',
                                        fontWeight: 'bold',
                                        display: 'inline-flex',
                                        alignItems: 'center'
                                    }}>
                                        {t}
                                    </span>
                                ))}
                            </div>
                        </div>
                        <div className="col-price"><span className="price-text">{p.price.toLocaleString()} đ</span></div>
                        <div className="col-price"><span className="price-text">{p.categoryName}</span></div>
                        <div className="col-status"><span className={`status-pill ${p.status.toLowerCase()}`}>{p.status}</span></div>
                        <div className="col-stock">
                            <span className={`stock-badge ${p.available ? "is-on" : "is-off"}`}>
                                <i className={`fas fa-${p.available ? "check-circle" : "times-circle"}`}></i> {p.available ? "Available" : "Off"}
                            </span>
                        </div>
                        <div className="col-actions">
                            <button className="row-btn tag" onClick={(e) => openTagModal(e, p)} title="Assign Tags">
                                <i className="fas fa-tags"></i>
                            </button>
                            <button className="row-btn edit" onClick={(e) => openEditModal(e, p)}><i className="fas fa-pen"></i></button>
                            <button className="row-btn delete" onClick={(e) => openDeleteModal(e, p.id)}><i className="fas fa-trash"></i></button>
                            <button className="row-btn delete" onClick={(e) => openImageProduct(e, p)}><i className="fas fa-image"></i></button>
                        </div>
                    </div>
                ))}
                {filteredProducts.length === 0 && <div className="empty-state">No products found. ☕</div>}
            </div>

            {activeProduct && (
                <div className="admin-card" style={{ padding: "20px" }}>
                    <ProductImageManager
                        product={activeProduct}
                        onClose={() => setActiveProduct(null)}
                    />
                </div>
            )}

            {/* FORM MODAL (CREATE & UPDATE) */}
            {showModal && (
                <div className="modal-overlay" onClick={() => setShowModal(false)}>
                    <div className="modal" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h3>{isEditMode ? "🔄 Update Product" : "✨ New Product"}</h3>
                            <button className="close-modal" onClick={() => setShowModal(false)}>&times;</button>
                        </div>
                        <div className="modal-body">
                            <div className={`input-group ${errors.name ? "has-error" : ""}`}>
                                <label>Product Name *</label>
                                {errors.name && <span className="error-text-top">{errors.name}</span>}
                                <input name="name" placeholder="Enter product name" onChange={handleChange} value={form.name} />
                            </div>
                            <div className="input-row">
                                <div className={`input-group ${errors.sku ? "has-error" : ""}`}>
                                    <label>SKU *</label>
                                    {errors.sku && <span className="error-text-top">{errors.sku}</span>}
                                    <input name="sku" placeholder="SKU-123" onChange={handleChange} value={form.sku} />
                                </div>
                                <div className={`input-group ${errors.price ? "has-error" : ""}`}>
                                    <label>Price (VNĐ) *</label>
                                    {errors.price && <span className="error-text-top">{errors.price}</span>}
                                    <input name="price" type="number" placeholder="0.00" onChange={handleChange} value={form.price} />
                                </div>
                            </div>
                            <div className={`input-group ${errors.category_id ? "has-error" : ""}`}>
                                <label>Category *</label>
                                {errors.category_id && <span className="error-text-top">{errors.category_id}</span>}
                                <select name="category_id" onChange={handleChange} value={form.category_id}>
                                    <option value="">Select Category</option>
                                    {categories.map((c) => (<option key={c.id} value={c.id}>{c.name}</option>))}
                                </select>
                            </div>
                            <div className="input-group">
                                <label>Description</label>
                                <textarea name="description" placeholder="A brief description..." onChange={handleChange} value={form.description} />
                            </div>
                            <div className="input-group" style={{ marginBottom: "15px" }}>
                                <label style={{ fontWeight: 'bold', color: '#512a10', display: 'block', marginBottom: '8px' }}>Product Tags</label>
                                <div className="tags-grid" style={{
                                    display: 'grid',
                                    gridTemplateColumns: 'repeat(auto-fill, minmax(130px, 1fr))',
                                    gap: '1rem',
                                    border: '1.5px solid #efe5db',
                                    padding: '12px',
                                    borderRadius: '8px',
                                    maxHeight: '120px',
                                    overflowY: 'auto'
                                }}>
                                    {tags.map((tag) => (
                                        <label key={tag.id} className="tag-checkbox-label" style={{
                                            display: 'flex',
                                            alignItems: 'center',
                                            gap: '8px',
                                            fontSize: '1.3rem',
                                            cursor: 'pointer',
                                            userSelect: 'none'
                                        }}>
                                            <input
                                                type="checkbox"
                                                value={tag.id}
                                                checked={selectedTags.includes(String(tag.id))}
                                                onChange={() => handleTagSelection(String(tag.id))}
                                                style={{ width: '16px', height: '16px', cursor: 'pointer' }}
                                            />
                                            <span className="tag-checkbox-text">{tag.name}</span>
                                        </label>
                                    ))}
                                </div>
                            </div>
                            <label className="checkbox-container">
                                <input type="checkbox" name="is_available" checked={form.is_available} onChange={handleChange} />
                                <span className="checkmark"></span> Available for sale
                            </label>
                        </div>
                        <div className="modal-actions">
                            <button onClick={() => setShowModal(false)} className="cancel-btn">Discard</button>
                            <button onClick={handleSubmit} className="save-btn">
                                {isEditMode ? "Save Changes" : "Create Product"}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* CUSTOM DELETE MODAL */}
            {showDeleteModal && (
                <div className="modal-overlay delete-overlay" onClick={() => setShowDeleteModal(false)}>
                    <div className="confirm-modal" onClick={(e) => e.stopPropagation()}>
                        <div className="confirm-icon"><i className="fas fa-exclamation-triangle"></i></div>
                        <h3>Are you sure?</h3>
                        <p>This action cannot be undone.</p>
                        <div className="confirm-actions">
                            <button className="cancel-confirm-btn" onClick={() => setShowDeleteModal(false)}>No, Cancel</button>
                            <button className="delete-confirm-btn" onClick={handleDelete}>Yes, Delete it!</button>
                        </div>
                    </div>
                </div>
            )}

            {/* SUCCESS POPUP */}
            {successPopup.show && (
                <div className="modal-overlay success-overlay">
                    <div className="success-popup">
                        <div className="success-icon-circle"><i className="fas fa-check"></i></div>
                        <h4>{successPopup.message}</h4>
                        <div className="success-progress-bar"></div>
                              </div>
                </div>
            )}

            {/* TAG ASSIGNMENT MODAL */}
            {showTagModal && (
                <div className="modal-overlay" onClick={() => setShowTagModal(false)} style={{
                    position: 'fixed',
                    top: 0,
                    left: 0,
                    right: 0,
                    bottom: 0,
                    background: 'rgba(0, 0, 0, 0.5)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    zIndex: 1000
                }}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()} style={{
                        background: 'white',
                        borderRadius: '12px',
                        width: '90%',
                        maxWidth: '600px',
                        maxHeight: '80vh',
                        overflowY: 'auto',
                        boxShadow: '0 20px 25px rgba(0, 0, 0, 0.15)'
                    }}>
                        <div className="modal-header" style={{ padding: "20px" }}>
                            <h3>Assign Tags</h3>
                            <button className="close-btn" onClick={() => setShowTagModal(false)}>
                                <i className="fas fa-times"></i>
                            </button>
                        </div>
                        <div className="modal-body" style={{ padding: "0 20px" }}>
                            <div className="tag-selection-section">
                                <div className="tags-grid" style={{
                                    display: 'grid',
                                    gridTemplateColumns: 'repeat(auto-fill, minmax(130px, 1fr))',
                                    gap: '1.5rem',
                                    padding: '10px 0 20px 0'
                                }}>
                                    {tags.map((tag) => (
                                        <label key={tag.id} className="tag-checkbox-label" style={{
                                            display: 'flex',
                                            alignItems: 'center',
                                            gap: '8px',
                                            fontSize: '1.4rem',
                                            cursor: 'pointer',
                                            userSelect: 'none'
                                        }}>
                                            <input
                                                type="checkbox"
                                                value={tag.id}
                                                checked={selectedTags.includes(String(tag.id))}
                                                onChange={() => handleTagSelection(String(tag.id))}
                                                style={{ width: '18px', height: '18px', cursor: 'pointer' }}
                                            />
                                            <span className="tag-checkbox-text">{tag.name}</span>
                                        </label>
                                    ))}
                                </div>
                            </div>
                        </div>
                        <div className="modal-actions" style={{
                            padding: "20px",
                            borderTop: "1px solid #eee",
                            display: "flex",
                            justifyContent: "flex-end",
                            gap: "1.5rem"
                        }}>
                            <button onClick={() => setShowTagModal(false)} style={{
                                padding: "1rem 2rem",
                                backgroundColor: "#eee",
                                color: "#333",
                                border: "none",
                                borderRadius: "8px",
                                fontSize: "1.4rem",
                                fontWeight: "bold",
                                cursor: "pointer"
                            }}>Cancel</button>
                            <button onClick={handleAssignTags} style={{
                                padding: "1rem 2.5rem",
                                backgroundColor: "#512a10",
                                color: "white",
                                border: "none",
                                borderRadius: "8px",
                                fontSize: "1.4rem",
                                fontWeight: "bold",
                                cursor: "pointer"
                            }}>Assign Tags</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ProductPage;
