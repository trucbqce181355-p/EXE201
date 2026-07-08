import React, { useEffect, useState } from "react";
import { productApi } from "../services/api";
import Swal from "sweetalert2";
import "@fortawesome/fontawesome-free/css/all.min.css";

const slugify = (text) => {
  return text
    .toString()
    .toLowerCase()
    .trim()
    .replace(/\s+/g, "-") // Replace spaces with -
    .replace(/[^\w\-]+/g, "") // Remove all non-word chars
    .replace(/\-\-+/g, "-"); // Replace multiple - with single -
};

function TagManage() {
  const [tags, setTags] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  
  // Modal State
  const [showModal, setShowModal] = useState(false);
  const [isEditMode, setIsEditMode] = useState(false);
  const [editingTagId, setEditingTagId] = useState(null);
  
  // Form State
  const [formState, setFormState] = useState({
    name: "",
    slug: "",
    description: "",
    active: true,
  });

  const fetchTags = async () => {
    try {
      setLoading(true);
      const res = await productApi.getAllTags();
      setTags(res?.data || []);
    } catch (error) {
      console.error("Error fetching tags:", error);
      Swal.fire({
        icon: "error",
        title: "Lỗi tải nhãn",
        text: error?.response?.data?.message || "Không thể tải danh sách nhãn sản phẩm.",
        confirmButtonColor: "#512a10",
      });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTags();
  }, []);

  const handleNameChange = (e) => {
    const name = e.target.value;
    setFormState((prev) => ({
      ...prev,
      name,
      slug: isEditMode ? prev.slug : slugify(name),
    }));
  };

  const handleOpenCreateModal = () => {
    setFormState({
      name: "",
      slug: "",
      description: "",
      active: true,
    });
    setIsEditMode(false);
    setEditingTagId(null);
    setShowModal(true);
  };

  const handleOpenEditModal = (tag) => {
    setFormState({
      name: tag.name,
      slug: tag.slug || slugify(tag.name),
      description: tag.description || "",
      active: tag.active !== undefined ? tag.active : true,
    });
    setIsEditMode(true);
    setEditingTagId(tag.id);
    setShowModal(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formState.name.trim() || !formState.slug.trim()) {
      Swal.fire({
        icon: "warning",
        title: "Thông tin không hợp lệ",
        text: "Tên nhãn và Slug không được để trống!",
        confirmButtonColor: "#512a10",
      });
      return;
    }

    try {
      if (isEditMode) {
        await productApi.updateTag(editingTagId, formState);
        Swal.fire({
          icon: "success",
          title: "Cập nhật thành công!",
          text: `Đã cập nhật nhãn "${formState.name}".`,
          confirmButtonColor: "#512a10",
        });
      } else {
        await productApi.createTag(formState);
        Swal.fire({
          icon: "success",
          title: "Thêm thành công!",
          text: `Đã thêm nhãn mới "${formState.name}".`,
          confirmButtonColor: "#512a10",
        });
      }
      setShowModal(false);
      fetchTags();
    } catch (error) {
      console.error("Error saving tag:", error);
      Swal.fire({
        icon: "error",
        title: "Lỗi lưu nhãn",
        text: error?.response?.data?.message || "Đã xảy ra lỗi trong quá trình lưu thông tin.",
        confirmButtonColor: "#512a10",
      });
    }
  };

  const handleDelete = async (tag) => {
    const result = await Swal.fire({
      title: "Xác nhận xóa?",
      text: `Bạn có chắc chắn muốn xóa nhãn "${tag.name}"? Hành động này sẽ hủy kích hoạt nhãn.`,
      icon: "warning",
      showCancelButton: true,
      confirmButtonColor: "#d33",
      cancelButtonColor: "#3085d6",
      confirmButtonText: "Xóa",
      cancelButtonText: "Hủy",
    });

    if (result.isConfirmed) {
      try {
        await productApi.deleteTag(tag.id);
        Swal.fire({
          icon: "success",
          title: "Đã xóa!",
          text: `Nhãn "${tag.name}" đã được xóa thành công.`,
          confirmButtonColor: "#512a10",
        });
        fetchTags();
      } catch (error) {
        console.error("Error deleting tag:", error);
        Swal.fire({
          icon: "error",
          title: "Lỗi xóa nhãn",
          text: error?.response?.data?.message || "Không thể xóa nhãn sản phẩm này.",
          confirmButtonColor: "#512a10",
        });
      }
    }
  };

  const filteredTags = tags.filter((tag) => {
    const query = searchTerm.toLowerCase();
    return (
      tag.name.toLowerCase().includes(query) ||
      (tag.description && tag.description.toLowerCase().includes(query)) ||
      (tag.slug && tag.slug.toLowerCase().includes(query))
    );
  });

  return (
    <div className="admin-page" style={{ padding: "30px" }}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "30px" }}>
        <div>
          <h2 style={{ fontSize: "2.8rem", color: "#512a10", fontWeight: 800 }}>Quản lý Tags (Nhãn sản phẩm)</h2>
          <p style={{ fontSize: "1.4rem", color: "#888" }}>Xem, tạo mới, chỉnh sửa và cấu hình nhãn phân loại sản phẩm.</p>
        </div>
        <button
          onClick={handleOpenCreateModal}
          style={{
            padding: "12px 24px",
            backgroundColor: "#512a10",
            color: "white",
            border: "none",
            borderRadius: "8px",
            fontSize: "1.5rem",
            fontWeight: "bold",
            cursor: "pointer",
            display: "flex",
            alignItems: "center",
            gap: "8px",
            boxShadow: "0 4px 6px rgba(81, 42, 16, 0.15)",
          }}
        >
          <i className="fas fa-plus"></i> Thêm Tag Mới
        </button>
      </div>

      {/* FILTER & SEARCH */}
      <div
        className="admin-card"
        style={{
          background: "white",
          borderRadius: "12px",
          padding: "20px",
          marginBottom: "30px",
          boxShadow: "0 2px 4px rgba(0,0,0,0.02)",
        }}
      >
        <div style={{ position: "relative", maxWidth: "400px" }}>
          <input
            type="text"
            placeholder="Tìm kiếm nhãn theo tên, slug, mô tả..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            style={{
              width: "100%",
              height: "4.5rem",
              padding: "0 1.5rem 0 4rem",
              borderRadius: "8px",
              border: "1.5px solid #efe5db",
              fontSize: "1.4rem",
              outline: "none",
            }}
          />
          <i
            className="fas fa-search"
            style={{
              position: "absolute",
              left: "15px",
              top: "50%",
              transform: "translateY(-50%)",
              fontSize: "1.4rem",
              color: "#aaa",
            }}
          ></i>
        </div>
      </div>

      {/* TAGS LIST TABLE */}
      <div
        className="admin-card"
        style={{
          background: "white",
          borderRadius: "12px",
          padding: "20px",
          boxShadow: "0 4px 10px rgba(0,0,0,0.03)",
        }}
      >
        {loading ? (
          <div style={{ textAlign: "center", padding: "50px", fontSize: "1.8rem", color: "#512a10" }}>
            <i className="fas fa-spinner fa-spin" style={{ marginRight: "10px" }}></i> Đang tải danh sách nhãn...
          </div>
        ) : filteredTags.length === 0 ? (
          <div style={{ textAlign: "center", padding: "50px", fontSize: "1.8rem", color: "#666" }}>
            <i className="fas fa-tags" style={{ fontSize: "4rem", color: "#ccc", marginBottom: "15px", display: "block" }}></i>
            Chưa có nhãn sản phẩm nào khớp với tìm kiếm.
          </div>
        ) : (
          <div style={{ overflowX: "auto" }}>
            <table className="admin-table" style={{ width: "100%", borderCollapse: "collapse", fontSize: "1.5rem" }}>
              <thead>
                <tr style={{ borderBottom: "2px solid #efe5db", textAlign: "left", color: "#888", fontWeight: "bold" }}>
                  <th style={{ padding: "15px 20px" }}>ID</th>
                  <th style={{ padding: "15px 20px" }}>Tên nhãn</th>
                  <th style={{ padding: "15px 20px" }}>Slug</th>
                  <th style={{ padding: "15px 20px" }}>Mô tả</th>
                  <th style={{ padding: "15px 20px" }}>Trạng thái</th>
                  <th style={{ padding: "15px 20px", textAlign: "right" }}>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {filteredTags.map((tag) => (
                  <tr
                    key={tag.id}
                    style={{
                      borderBottom: "1px solid #f6eff2",
                      transition: "background 0.2s",
                    }}
                    onMouseEnter={(e) => (e.currentTarget.style.backgroundColor = "#faf6f4")}
                    onMouseLeave={(e) => (e.currentTarget.style.backgroundColor = "transparent")}
                  >
                    <td style={{ padding: "15px 20px", color: "#888" }}>{tag.id}</td>
                    <td style={{ padding: "15px 20px", fontWeight: "bold", color: "#512a10" }}>
                      <span
                        style={{
                          backgroundColor: "#fdfaf7",
                          border: "1.5px solid #512a10",
                          color: "#512a10",
                          padding: "4px 10px",
                          borderRadius: "4px",
                          fontWeight: "bold",
                        }}
                      >
                        {tag.name}
                      </span>
                    </td>
                    <td style={{ padding: "15px 20px", color: "#666", fontFamily: "monospace" }}>{tag.slug}</td>
                    <td style={{ padding: "15px 20px", color: "#666" }}>{tag.description || <em style={{ color: "#aaa" }}>Không có mô tả</em>}</td>
                    <td style={{ padding: "15px 20px" }}>
                      <span
                        style={{
                          padding: "4px 8px",
                          borderRadius: "4px",
                          fontSize: "1.2rem",
                          fontWeight: "bold",
                          backgroundColor: tag.active ? "#e8f5e9" : "#ffebee",
                          color: tag.active ? "#2e7d32" : "#c62828",
                        }}
                      >
                        {tag.active ? "Đang hoạt động" : "Ngưng hoạt động"}
                      </span>
                    </td>
                    <td style={{ padding: "15px 20px", textAlign: "right" }}>
                      <div style={{ display: "flex", justifyContent: "flex-end", gap: "10px" }}>
                        <button
                          onClick={() => handleOpenEditModal(tag)}
                          style={{
                            border: "none",
                            background: "none",
                            color: "#3498db",
                            cursor: "pointer",
                            fontSize: "1.6rem",
                          }}
                          title="Sửa nhãn"
                        >
                          <i className="fas fa-pen"></i>
                        </button>
                        <button
                          onClick={() => handleDelete(tag)}
                          style={{
                            border: "none",
                            background: "none",
                            color: "#e74c3c",
                            cursor: "pointer",
                            fontSize: "1.6rem",
                          }}
                          title="Xóa nhãn"
                        >
                          <i className="fas fa-trash-alt"></i>
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* CREATE & EDIT TAG MODAL */}
      {showModal && (
        <div
          style={{
            position: "fixed",
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundColor: "rgba(0, 0, 0, 0.5)",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            zIndex: 2000,
          }}
          onClick={() => setShowModal(false)}
        >
          <div
            style={{
              backgroundColor: "white",
              borderRadius: "12px",
              width: "90%",
              maxWidth: "500px",
              boxShadow: "0 20px 25px rgba(0, 0, 0, 0.15)",
              overflow: "hidden",
            }}
            onClick={(e) => e.stopPropagation()}
          >
            <div
              style={{
                padding: "20px",
                borderBottom: "1px solid #eee",
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
              }}
            >
              <h3 style={{ fontSize: "2rem", color: "#512a10", fontWeight: "bold" }}>
                {isEditMode ? "🔄 Cập nhật Tag" : "✨ Thêm Tag Mới"}
              </h3>
              <button
                onClick={() => setShowModal(false)}
                style={{ background: "none", border: "none", fontSize: "1.8rem", cursor: "pointer", color: "#888" }}
              >
                &times;
              </button>
            </div>
            <form onSubmit={handleSubmit}>
              <div style={{ padding: "20px", display: "flex", flexDirection: "column", gap: "20px", fontSize: "1.5rem" }}>
                <div style={{ display: "flex", flexDirection: "column", gap: "8px" }}>
                  <label style={{ fontWeight: "bold", color: "#512a10" }}>Tên nhãn *</label>
                  <input
                    type="text"
                    required
                    value={formState.name}
                    onChange={handleNameChange}
                    placeholder="Ví dụ: Best Seller, Mới Nhất..."
                    style={{
                      height: "4.5rem",
                      padding: "0 1.5rem",
                      borderRadius: "8px",
                      border: "1.5px solid #efe5db",
                      fontSize: "1.4rem",
                      outline: "none",
                    }}
                  />
                </div>
                <div style={{ display: "flex", flexDirection: "column", gap: "8px" }}>
                  <label style={{ fontWeight: "bold", color: "#512a10" }}>Slug *</label>
                  <input
                    type="text"
                    required
                    value={formState.slug}
                    onChange={(e) => setFormState((prev) => ({ ...prev, slug: e.target.value }))}
                    placeholder="Ví dụ: best-seller, moi-nhat..."
                    style={{
                      height: "4.5rem",
                      padding: "0 1.5rem",
                      borderRadius: "8px",
                      border: "1.5px solid #efe5db",
                      fontSize: "1.4rem",
                      outline: "none",
                    }}
                  />
                </div>
                <div style={{ display: "flex", flexDirection: "column", gap: "8px" }}>
                  <label style={{ fontWeight: "bold", color: "#512a10" }}>Mô tả</label>
                  <textarea
                    value={formState.description}
                    onChange={(e) => setFormState((prev) => ({ ...prev, description: e.target.value }))}
                    placeholder="Mô tả công dụng hoặc phạm vi áp dụng nhãn..."
                    rows={3}
                    style={{
                      padding: "1.2rem 1.5rem",
                      borderRadius: "8px",
                      border: "1.5px solid #efe5db",
                      fontSize: "1.4rem",
                      outline: "none",
                      resize: "none",
                    }}
                  />
                </div>
                <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
                  <input
                    type="checkbox"
                    id="tag-active"
                    checked={formState.active}
                    onChange={(e) => setFormState((prev) => ({ ...prev, active: e.target.checked }))}
                    style={{ width: "20px", height: "20px", cursor: "pointer" }}
                  />
                  <label htmlFor="tag-active" style={{ fontWeight: "bold", color: "#512a10", cursor: "pointer" }}>
                    Kích hoạt hoạt động
                  </label>
                </div>
              </div>
              <div
                style={{
                  padding: "20px",
                  borderTop: "1px solid #eee",
                  display: "flex",
                  justifyContent: "flex-end",
                  gap: "15px",
                }}
              >
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  style={{
                    padding: "10px 20px",
                    backgroundColor: "#eee",
                    border: "none",
                    borderRadius: "8px",
                    fontSize: "1.4rem",
                    fontWeight: "bold",
                    cursor: "pointer",
                  }}
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  style={{
                    padding: "10px 25px",
                    backgroundColor: "#512a10",
                    color: "white",
                    border: "none",
                    borderRadius: "8px",
                    fontSize: "1.4rem",
                    fontWeight: "bold",
                    cursor: "pointer",
                  }}
                >
                  {isEditMode ? "Cập nhật" : "Lưu lại"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default TagManage;
