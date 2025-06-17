"use client"

import { useState, useContext } from "react"
import { useNavigate } from "react-router-dom"
import { userAPI } from "../api"
import { setNickname, setRole } from "../utils/storage"
import "./UserTypeSelectionPage.css"

const UserTypeSelectionPage = () => {
  const navigate = useNavigate()
  const [formData, setFormData] = useState({
    role: "",
    nickname: "",
  })
  const [loading, setLoading] = useState(false)

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }))
  }

  const handleRoleSelect = (role) => {
    setFormData((prev) => ({
      ...prev,
      role,
    }))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()

    if (!formData.role) {
      alert("사용자 유형을 선택해주세요.")
      return
    }

    try {
      setLoading(true)

      const response = await userAPI.signup(formData)
      setNickname(response.data.nickname);
      setRole(response.data.role);
      console.log(response.data.accessToken)
      setAccessToken(response.data.accessToken);
      // Update user context
      
      navigate("/")
    } catch (error) {
      console.error("Error updating user info:", error)
      alert("회원가입에 실패했습니다.")
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="user-type-selection-page">
      <div className="selection-container">
        <h1 className="selection-title">회원가입</h1>
        <p className="selection-subtitle">매장 플랫폼에서 새로운 사업을 함께하세요</p>

        <div className="selection-card">
          <h2 className="card-title">사용자 유형 선택</h2>

          <div className="role-selection">
            <div
              className={`role-card ${formData.role === "ROLE_STORE" ? "selected" : ""}`}
              onClick={() => handleRoleSelect("ROLE_STORE")}
            >
              <div className="role-icon store-icon">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M13 10V3L4 14h7v7l9-11h-7z" />
                </svg>
              </div>
              <h3>점주</h3>
              <p>매장을 운영하는 사업자</p>
            </div>

            <div
              className={`role-card ${formData.role === "ROLE_IP" ? "selected" : ""}`}
              onClick={() => handleRoleSelect("ROLE_IP")}
            >
              <div className="role-icon ip-icon">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm-1-13h2v6h-2zm0 8h2v2h-2z" />
                </svg>
              </div>
              <h3>IP 제공자</h3>
              <p>아이디어와 콘텐츠를 제공</p>
            </div>
          </div>

          <form onSubmit={handleSubmit} className="user-form">
            <div className="form-group">
              <label htmlFor="nickname">닉네임</label>
              <input
                type="text"
                id="nickname"
                name="nickname"
                value={formData.nickname}
                onChange={handleChange}
                className="form-control"
                placeholder="닉네임을 입력해주세요"
                required
              />
            </div>

            <button type="submit" className="submit-btn" disabled={!formData.role || loading}>
              {loading ? "처리 중..." : "가입하기"}
            </button>
          </form>
        </div>
      </div>
    </div>
  )
}

export default UserTypeSelectionPage
