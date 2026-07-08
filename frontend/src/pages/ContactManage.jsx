import { useEffect, useState, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import { contactApi } from "../services/api";
import "@fortawesome/fontawesome-free/css/all.min.css";

export default function ContactManage() {
  const navigate = useNavigate();
  const [contacts, setContacts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedContact, setSelectedContact] = useState(null);

  const loadContacts = async () => {
    setLoading(true);
    setError("");
    try {
      const response = await contactApi.getContacts();
      setContacts(response.data || []);
    } catch (err) {
      setError(err?.response?.data?.message || "Failed to load contact messages.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const token = localStorage.getItem("accessToken");
    if (!token) {
      navigate("/", { replace: true });
      return;
    }
    loadContacts();
  }, []);

  const filteredContacts = useMemo(() => {
    const term = searchTerm.trim().toLowerCase();
    if (!term) return contacts;
    return contacts.filter(
      (c) =>
        c.name.toLowerCase().includes(term) ||
        c.email.toLowerCase().includes(term) ||
        c.phone.includes(term) ||
        c.message.toLowerCase().includes(term)
    );
  }, [contacts, searchTerm]);

  const formatDate = (dateString) => {
    if (!dateString) return "-";
    try {
      const d = new Date(dateString);
      return d.toLocaleString("vi-VN");
    } catch {
      return dateString;
    }
  };

  return (
    <div style={{ maxWidth: 1180, margin: "0 auto", paddingBottom: 40 }}>
      {/* HEADER CARD */}
      <div
        className="form-card"
        style={{
          padding: 22,
          borderRadius: 14,
          marginBottom: 18,
          boxShadow: "0 8px 24px rgba(15,23,42,0.06)",
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          flexWrap: "wrap",
          gap: 15
        }}
      >
        <div>
          <h1 style={{ fontSize: 30, margin: 0, color: "#111827" }}>Customer Contacts</h1>
          <p style={{ fontSize: 15, margin: "6px 0 0", color: "#6b7280" }}>
            Review feedback and inquiry messages sent by customers from the homepage contact form.
          </p>
        </div>
        <div style={{ display: "flex", gap: 10, alignItems: "center" }}>
          <div style={{ position: "relative" }}>
            <i
              className="fas fa-search"
              style={{
                position: "absolute",
                left: 12,
                top: "50%",
                transform: "translateY(-50%)",
                color: "#9ca3af",
                fontSize: 14
              }}
            ></i>
            <input
              type="text"
              placeholder="Search contacts..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              style={{
                padding: "8px 12px 8px 34px",
                borderRadius: 8,
                border: "1px solid #d1d5db",
                fontSize: 14,
                width: 260,
                outline: "none"
              }}
            />
          </div>
          <button
            type="button"
            className="admin-secondary-btn"
            onClick={loadContacts}
            style={{ display: "flex", alignItems: "center", gap: 6, padding: "8px 16px" }}
          >
            <i className="fas fa-sync-alt"></i> Refresh
          </button>
        </div>
      </div>

      {error && (
        <div style={{ marginBottom: 12, color: "#b91c1c", fontWeight: 600, padding: "0 10px" }}>
          {error}
        </div>
      )}

      {/* TABLE */}
      <div
        className="table-responsive"
        style={{ borderRadius: 12, overflow: "hidden", boxShadow: "0 6px 18px rgba(15,23,42,0.05)" }}
      >
        <table className="admin-table" style={{ fontSize: 15 }}>
          <thead style={{ background: "#eef2ff" }}>
            <tr>
              <th style={{ padding: "14px 12px", width: "80px" }}>ID</th>
              <th style={{ padding: "14px 12px", width: "180px" }}>Customer Name</th>
              <th style={{ padding: "14px 12px", width: "200px" }}>Email</th>
              <th style={{ padding: "14px 12px", width: "150px" }}>Phone</th>
              <th style={{ padding: "14px 12px" }}>Message</th>
              <th style={{ padding: "14px 12px", width: "180px" }}>Sent At</th>
              <th style={{ padding: "14px 12px", width: "120px" }}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {!loading && filteredContacts.length === 0 && (
              <tr>
                <td colSpan={7} style={{ textAlign: "center", opacity: 0.7, padding: 24 }}>
                  No contact messages found.
                </td>
              </tr>
            )}
            {loading && (
              <tr>
                <td colSpan={7} style={{ textAlign: "center", padding: 24 }}>
                  <i className="fas fa-spinner fa-spin" style={{ marginRight: 8 }}></i> Loading messages...
                </td>
              </tr>
            )}
            {!loading &&
              filteredContacts.map((contact) => (
                <tr key={contact.id}>
                  <td style={{ padding: "12px 12px" }}>{contact.id}</td>
                  <td style={{ padding: "12px 12px", fontWeight: 600 }}>{contact.name}</td>
                  <td style={{ padding: "12px 12px" }}>
                    <a href={`mailto:${contact.email}`} style={{ color: "#3e704d", textDecoration: "none" }}>
                      {contact.email}
                    </a>
                  </td>
                  <td style={{ padding: "12px 12px" }}>
                    <a href={`tel:${contact.phone}`} style={{ color: "#4b5563", textDecoration: "none" }}>
                      {contact.phone}
                    </a>
                  </td>
                  <td style={{ padding: "12px 12px", maxWidth: "250px", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                    {contact.message}
                  </td>
                  <td style={{ padding: "12px 12px" }}>{formatDate(contact.createdAt)}</td>
                  <td style={{ padding: "12px 12px" }}>
                    <button
                      type="button"
                      className="edit-btn"
                      onClick={() => setSelectedContact(contact)}
                      style={{ padding: "4px 8px", background: "#3e704d", color: "#fff", border: "none", borderRadius: 4, cursor: "pointer" }}
                    >
                      <i className="far fa-envelope-open" style={{ marginRight: 4 }}></i> View
                    </button>
                  </td>
                </tr>
              ))}
          </tbody>
        </table>
      </div>

      {/* DETAIL MODAL POPUP */}
      {selectedContact && (
        <div
          style={{
            position: "fixed",
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundColor: "rgba(0,0,0,0.5)",
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            zIndex: 9999
          }}
        >
          <div
            style={{
              backgroundColor: "#fff",
              padding: 24,
              borderRadius: 12,
              width: "90%",
              maxWidth: "600px",
              boxShadow: "0 20px 25px -5px rgba(0,0,0,0.1), 0 10px 10px -5px rgba(0,0,0,0.04)"
            }}
          >
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", borderBottom: "1px solid #e5e7eb", paddingBottom: 12, marginBottom: 16 }}>
              <h3 style={{ margin: 0, fontSize: 20, color: "#111827" }}>Contact Message Details</h3>
              <button
                onClick={() => setSelectedContact(null)}
                style={{ background: "none", border: "none", fontSize: 20, cursor: "pointer", color: "#9ca3af" }}
              >
                &times;
              </button>
            </div>
            
            <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
              <div>
                <strong>Customer Name:</strong>
                <div style={{ padding: "8px 12px", background: "#f9fafb", borderRadius: 6, marginTop: 4 }}>{selectedContact.name}</div>
              </div>
              <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 12 }}>
                <div>
                  <strong>Email:</strong>
                  <div style={{ padding: "8px 12px", background: "#f9fafb", borderRadius: 6, marginTop: 4 }}>{selectedContact.email}</div>
                </div>
                <div>
                  <strong>Phone Number:</strong>
                  <div style={{ padding: "8px 12px", background: "#f9fafb", borderRadius: 6, marginTop: 4 }}>{selectedContact.phone}</div>
                </div>
              </div>
              <div>
                <strong>Sent Date:</strong>
                <div style={{ padding: "8px 12px", background: "#f9fafb", borderRadius: 6, marginTop: 4 }}>{formatDate(selectedContact.createdAt)}</div>
              </div>
              <div>
                <strong>Message Content:</strong>
                <div style={{ padding: "12px", background: "#f9fafb", borderRadius: 6, marginTop: 4, maxHeight: "200px", overflowY: "auto", whiteSpace: "pre-wrap", lineHeight: 1.5 }}>
                  {selectedContact.message}
                </div>
              </div>
            </div>

            <div style={{ display: "flex", justifyContent: "flex-end", marginTop: 20, borderTop: "1px solid #e5e7eb", paddingTop: 12 }}>
              <button
                type="button"
                onClick={() => setSelectedContact(null)}
                style={{
                  padding: "8px 16px",
                  borderRadius: 6,
                  border: "1px solid #d1d5db",
                  backgroundColor: "#fff",
                  cursor: "pointer",
                  fontWeight: 600
                }}
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
