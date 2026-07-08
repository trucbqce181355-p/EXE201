import { useEffect, useState } from "react";
import axios from "axios";

const API_URL = "http://localhost:8081/api/permissions";
const getToken = () => localStorage.getItem("accessToken");

export default function PermissionForm({ editing, onSuccess }) {
    const [form, setForm] = useState({
        name: "",
        resource: "",
        action: "",
        description: ""
    });

    const [loading, setLoading] = useState(false);

    const [fieldError, setFieldError] = useState({
        name: "",
        resource: ""
    });

    /* ===================== INIT FORM ===================== */
    useEffect(() => {
        if (editing) {
            setForm({
                name: editing.name || "",
                resource: editing.resource || "",
                action: editing.action || "",
                description: editing.description || ""
            });
            setFieldError({ name: "", resource: "", action: "" });
        } else {
            setForm({
                name: "",
                resource: "",
                action: "",
                description: ""
            });
            setFieldError({ name: "", resource: "", action: "" });
        }
    }, [editing]);

    /* ===================== HANDLERS ===================== */
    const handleChange = (e) => {
        const { name, value } = e.target;
        setForm(prev => ({ ...prev, [name]: value }));

        setFieldError(prev => ({ ...prev, [name]: "" }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setFieldError({ name: "", resource: "", action: "" });

        const token = getToken();
        if (!token) {
            setFieldError({ name: "You are not logged in" });
            return;
        }

        setLoading(true);

        try {
            if (editing) {
                // UPDATE: chỉ cho sửa description
                await axios.put(
                    `${API_URL}/${editing.id}`,
                    { description: form.description },
                    { headers: { Authorization: `Bearer ${token}` } }
                );
            } else {
                // CREATE
                await axios.post(
                    API_URL,
                    {
                        name: form.name,
                        resource: form.resource,
                        action: form.action,
                        description: form.description
                    },
                    { headers: { Authorization: `Bearer ${token}` } }
                );
            }

            onSuccess();

            if (!editing) {
                setForm({
                    name: "",
                    resource: "",
                    action: "",
                    description: ""
                });
            }

        } catch (err) {
            if (axios.isAxiosError(err)) {
                const status = err.response?.status;
                const msg = err.response?.data?.message;

                if (status === 409) {
                    if (msg && msg.toLowerCase().includes("name")) {
                        setFieldError({
                            name: "Tên quyền này đã tồn tại"
                        });
                    } else {
                        setFieldError({
                            resource: "Tài nguyên và hành động này đã tồn tại",
                            action: "Tài nguyên và hành động này đã tồn tại"
                        });
                    }
                } else {
                    setFieldError({
                        name: msg || "Lưu quyền thất bại"
                    });
                }
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <form className="permission-form" onSubmit={handleSubmit}>

            {/* ===== NAME ===== */}
            <label>
                Tên quyền
                <input
                    name="name"
                    value={form.name}
                    onChange={handleChange}
                    placeholder="USER_CREATE"
                    required
                    disabled={!!editing}
                    className={fieldError.name ? "input-error" : ""}
                />
                {fieldError.name && (
                    <small className="field-error">{fieldError.name}</small>
                )}
            </label>

            {/* ===== RESOURCE ===== */}
            <label>
                Tài nguyên
                <input
                    name="resource"
                    value={form.resource}
                    onChange={handleChange}
                    placeholder="USER / ROLE / PERMISSION"
                    required
                    disabled={!!editing}
                    className={fieldError.resource ? "input-error" : ""}
                />
                {fieldError.resource && (
                    <small className="field-error">{fieldError.resource}</small>
                )}
            </label>

            {/* ===== ACTION ===== */}
            <label>
                Hành động
                <input
                    name="action"
                    value={form.action}
                    onChange={handleChange}
                    placeholder="CREATE / READ / UPDATE / DELETE"
                    required
                    disabled={!!editing}
                    className={fieldError.action ? "input-error" : ""}
                />
                {fieldError.action && (
                    <small className="field-error">{fieldError.action}</small>
                )}
            </label>

            {/* ===== DESCRIPTION ===== */}
            <label>
                Mô tả
                <textarea
                    name="description"
                    value={form.description}
                    onChange={handleChange}
                    placeholder="Mô tả quyền"
                />
            </label>

            {/* ===== ACTIONS ===== */}
            <div className="form-actions">
                <button
                    className="btn-primary"
                    type="submit"
                    disabled={
                        loading ||
                        (!editing && (!form.name || !form.resource || !form.action))
                    }
                >
                    {loading ? "Đang lưu..." : editing ? "Cập nhật" : "Tạo mới"}
                </button>
            </div>
        </form>
    );
}