import { useEffect, useState } from "react";
import { blogApi } from "../services/api";
import Swal from "sweetalert2";

function BlogManagement() {
  const [posts, setPosts] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  
  // Form state for creating/editing
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalMode, setModalMode] = useState("create"); // "create" or "edit"
  const [editingPostId, setEditingPostId] = useState(null);
  const [formState, setFormState] = useState({
    title: "",
    author: "Admin",
    imageUrl: "/images/g-img-1.jpg",
    content: ""
  });

  const fetchPosts = async () => {
    setIsLoading(true);
    try {
      const res = await blogApi.getAll();
      const list = res?.data || res || [];
      setPosts(list);
    } catch (err) {
      console.error("Lỗi khi tải danh sách bài viết:", err);
      Swal.fire("Lỗi", "Không thể tải danh sách bài viết", "error");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchPosts();
  }, []);

  const handleOpenCreateModal = () => {
    setFormState({
      title: "",
      author: "Admin",
      imageUrl: "/images/g-img-1.jpg",
      content: ""
    });
    setModalMode("create");
    setIsModalOpen(true);
  };

  const handleOpenEditModal = (post) => {
    setFormState({
      title: post.title || "",
      author: post.author || "Admin",
      imageUrl: post.imageUrl || "/images/g-img-1.jpg",
      content: post.content || ""
    });
    setEditingPostId(post.id);
    setModalMode("edit");
    setIsModalOpen(true);
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormState((prev) => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formState.title.trim() || !formState.content.trim()) {
      Swal.fire("Thông báo", "Vui lòng nhập đầy đủ tiêu đề và liên kết chi tiết", "warning");
      return;
    }

    try {
      if (modalMode === "create") {
        await blogApi.create(formState);
        Swal.fire("Thành công", "Đã đăng bài viết mới", "success");
      } else {
        await blogApi.update(editingPostId, formState);
        Swal.fire("Thành công", "Đã cập nhật bài viết", "success");
      }
      setIsModalOpen(false);
      fetchPosts();
    } catch (err) {
      console.error("Lỗi khi lưu bài viết:", err);
      Swal.fire("Lỗi", "Không thể lưu bài viết. Vui lòng thử lại.", "error");
    }
  };

  const handleDeletePost = async (id) => {
    const result = await Swal.fire({
      title: "Xác nhận xóa?",
      text: "Bạn có chắc chắn muốn xóa bài viết này không? Hành động này không thể hoàn tác.",
      icon: "warning",
      showCancelButton: true,
      confirmButtonColor: "#d33",
      cancelButtonColor: "#3085d6",
      confirmButtonText: "Xóa ngay",
      cancelButtonText: "Hủy"
    });

    if (result.isConfirmed) {
      try {
        await blogApi.delete(id);
        Swal.fire("Đã xóa", "Bài viết đã được xóa thành công.", "success");
        fetchPosts();
      } catch (err) {
        console.error("Lỗi khi xóa bài viết:", err);
        Swal.fire("Lỗi", "Không thể xóa bài viết. Vui lòng thử lại.", "error");
      }
    }
  };

  const getAbsoluteImageUrl = (url) => {
    if (!url) return "/images/g-img-1.jpg";
    if (url.startsWith("http://") || url.startsWith("https://")) return url;
    if (url.startsWith("/")) return url;
    return "/" + url;
  };

  return (
    <div className="container-fluid py-4" style={{ fontSize: "1.6rem" }}>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <span className="text-muted text-uppercase" style={{ fontSize: "1.3rem" }}>Back Office</span>
          <h2 className="m-0" style={{ fontWeight: "bold", color: "#512a10" }}>Daily Posts Management</h2>
        </div>
        <button className="btn btn-primary btn-lg" onClick={handleOpenCreateModal} style={{ background: "#512a10", borderColor: "#512a10", fontSize: "1.5rem", padding: "1rem 2rem" }}>
          <i className="fas fa-plus-circle me-2"></i> Đăng bài viết mới
        </button>
      </div>

      <div className="card shadow-sm" style={{ borderRadius: "1rem", overflow: "hidden", border: "none" }}>
        <div className="card-header bg-white py-3">
          <h5 className="m-0 font-weight-bold" style={{ color: "#333", fontSize: "1.8rem" }}>Danh sách bài viết</h5>
        </div>
        <div className="card-body p-0">
          {isLoading ? (
            <div className="text-center py-5">
              <div className="spinner-border text-primary" role="status"></div>
              <p className="mt-3 text-muted">Đang tải danh sách bài viết...</p>
            </div>
          ) : posts.length === 0 ? (
            <div className="text-center py-5">
              <i className="fas fa-blog text-muted mb-3" style={{ fontSize: "4rem" }}></i>
              <p className="text-muted">Chưa có bài viết nào. Hãy thêm bài viết đầu tiên!</p>
            </div>
          ) : (
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0" style={{ fontSize: "1.5rem" }}>
                <thead className="table-light text-uppercase" style={{ fontSize: "1.3rem" }}>
                  <tr>
                    <th className="px-4 py-3" style={{ width: "80px" }}>Hình ảnh</th>
                    <th className="py-3">Tiêu đề</th>
                    <th className="py-3" style={{ width: "150px" }}>Tác giả</th>
                    <th className="py-3" style={{ width: "180px" }}>Ngày đăng</th>
                    <th className="px-4 py-3 text-end" style={{ width: "200px" }}>Hành động</th>
                  </tr>
                </thead>
                <tbody>
                  {posts.map((post) => (
                    <tr key={post.id}>
                      <td className="px-4 py-3">
                        <img 
                          src={getAbsoluteImageUrl(post.imageUrl)} 
                          alt={post.title} 
                          style={{ width: "60px", height: "60px", objectFit: "cover", borderRadius: "0.5rem" }} 
                          onError={(e) => { 
                            e.currentTarget.onerror = null;
                            e.currentTarget.src = "/images/g-img-1.jpg"; 
                          }}
                        />
                      </td>
                      <td className="py-3 font-weight-bold" style={{ color: "#512a10" }}>
                        <div>{post.title}</div>
                      </td>
                      <td className="py-3">{post.author || "Admin"}</td>
                      <td className="py-3">
                        {post.publishedAt ? new Date(post.publishedAt).toLocaleString("vi-VN") : "N/A"}
                      </td>
                      <td className="px-4 py-3 text-end">
                        <button 
                          className="btn btn-outline-primary btn-sm me-2" 
                          onClick={() => handleOpenEditModal(post)}
                          style={{ fontSize: "1.3rem" }}
                        >
                          <i className="fas fa-edit"></i> Sửa
                        </button>
                        <button 
                          className="btn btn-outline-danger btn-sm" 
                          onClick={() => handleDeletePost(post.id)}
                          style={{ fontSize: "1.3rem" }}
                        >
                          <i className="fas fa-trash-alt"></i> Xóa
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
      {/* Editor Modal */}
      {isModalOpen && (
        <div 
          style={{ 
            position: "fixed",
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundColor: "rgba(0,0,0,0.5)", 
            zIndex: 2000,
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            padding: "20px"
          }}
          onClick={() => setIsModalOpen(false)}
        >
          <div 
            style={{ 
              background: "#fff",
              borderRadius: "1rem", 
              overflow: "hidden", 
              border: "none",
              width: "100%",
              maxWidth: "800px",
              boxShadow: "0 10px 25px rgba(0,0,0,0.2)",
              display: "flex",
              flexDirection: "column",
              maxHeight: "90vh"
            }}
            onClick={(e) => e.stopPropagation()}
          >
            <div className="text-white p-4" style={{ background: "#512a10", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
              <h5 className="m-0" style={{ fontSize: "2rem", fontWeight: "bold" }}>
                {modalMode === "create" ? "Đăng bài viết mới" : "Chỉnh sửa bài viết"}
              </h5>
              <button 
                type="button" 
                onClick={() => setIsModalOpen(false)} 
                aria-label="Close" 
                style={{ fontSize: "2rem", background: "none", border: "none", color: "#fff", cursor: "pointer" }}
              >
                <i className="fas fa-times"></i>
              </button>
            </div>
            <form onSubmit={handleSubmit} style={{ display: "flex", flexDirection: "column", flex: 1, overflow: "hidden", margin: 0 }}>
              <div className="p-4" style={{ overflowY: "auto", flex: 1 }}>
                <div className="mb-3">
                  <label className="form-label" style={{ fontWeight: "bold", fontSize: "1.5rem" }}>Tiêu đề bài viết</label>
                  <input 
                    type="text" 
                    className="form-control form-control-lg" 
                    name="title" 
                    value={formState.title} 
                    onChange={handleInputChange} 
                    placeholder="Nhập tiêu đề..." 
                    required 
                    style={{ fontSize: "1.5rem" }}
                  />
                </div>
                
                <div className="row">
                  <div className="col-md-6 mb-3">
                    <label className="form-label" style={{ fontWeight: "bold", fontSize: "1.5rem" }}>Tác giả</label>
                    <input 
                      type="text" 
                      className="form-control" 
                      name="author" 
                      value={formState.author} 
                      onChange={handleInputChange} 
                      placeholder="Admin..."
                      style={{ fontSize: "1.5rem" }}
                    />
                  </div>
                  <div className="col-md-6 mb-3">
                    <label className="form-label" style={{ fontWeight: "bold", fontSize: "1.5rem" }}>Đường dẫn ảnh đại diện</label>
                    <input 
                      type="text" 
                      className="form-control" 
                      name="imageUrl" 
                      value={formState.imageUrl} 
                      onChange={handleInputChange} 
                      placeholder="/images/g-img-1.jpg..."
                      style={{ fontSize: "1.5rem" }}
                    />
                  </div>
                </div>

                {formState.imageUrl && (
                  <div className="mb-3 text-center">
                    <label className="form-label d-block" style={{ fontWeight: "bold", textAlign: "left", fontSize: "1.5rem" }}>Xem trước ảnh</label>
                    <img 
                      src={getAbsoluteImageUrl(formState.imageUrl)} 
                      alt="Preview" 
                      style={{ maxHeight: "200px", maxWidth: "100%", borderRadius: "0.5rem", border: "1px solid #ddd" }}
                      onError={(e) => { 
                        e.currentTarget.onerror = null;
                        e.currentTarget.style.display = "none"; 
                      }}
                      onLoad={(e) => { e.currentTarget.style.display = "inline-block"; }}
                    />
                  </div>
                )}

                <div className="mb-3">
                  <label className="form-label" style={{ fontWeight: "bold", fontSize: "1.5rem" }}>Liên kết chi tiết bài viết (Link)</label>
                  <input 
                    type="url"
                    className="form-control" 
                    name="content" 
                    value={formState.content} 
                    onChange={handleInputChange} 
                    placeholder="https://example.com/bai-viet-chi-tiet..." 
                    required 
                    style={{ fontSize: "1.5rem" }}
                  />
                </div>
              </div>
              <div className="bg-light p-3 text-end" style={{ borderTop: "1px solid #eee" }}>
                <button type="button" className="btn btn-secondary btn-lg me-2" onClick={() => setIsModalOpen(false)} style={{ fontSize: "1.5rem", padding: "0.8rem 2rem" }}>Hủy</button>
                <button type="submit" className="btn btn-primary btn-lg" style={{ background: "#512a10", borderColor: "#512a10", color: "#fff", fontSize: "1.5rem", padding: "0.8rem 2rem" }}>Lưu bài viết</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default BlogManagement;
