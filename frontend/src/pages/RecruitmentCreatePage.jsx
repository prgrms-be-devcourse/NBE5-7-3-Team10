"use client"

import { useState, useEffect } from "react"
import { useNavigate } from "react-router-dom"
import { recruitmentAPI, profileAPI } from "../api"
import { getUserInfo } from "../utils/storage"
import "./RecruitmentCreatePage.css"

const RecruitmentCreatePage = () => {
  const navigate = useNavigate()
  const user = getUserInfo()

  const [userProfiles, setUserProfiles] = useState([])
  const [loading, setLoading] = useState(true)

  const today = new Date()
  today.setDate(today.getDate() + 7)
  const defaultDeadline = today.toISOString().split("T")[0]

  const [formData, setFormData] = useState({
    profileId: "",
    title: "",
    description: "",
    deadline: defaultDeadline,
    status: "RECRUITING"
  })

  useEffect(() => {
    const fetchUserProfiles = async () => {
      try {
        const response = await profileAPI.getUserProfiles(user?.userId)
        setUserProfiles(response.data)
      } catch (error) {
        console.error("Error fetching user profiles:", error)
      } finally {
        setLoading(false)
      }
    }
    if (user?.userId) {
      fetchUserProfiles()
    }
  }, [user?.userId]) // ✅ 이렇게 변경

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()

    try {
      setLoading(true)

      await recruitmentAPI.createRecruitment({
        ...formData,
        deadline: `${formData.deadline}T00:00:00`,
        status: "RECRUITING",
      })

      alert("모집글이 등록되었습니다.")
      navigate("/recruitment")
    } catch (error) {
      console.error("Error creating recruitment:", error)
      alert("모집글 등록에 실패했습니다.")
    } finally {
      setLoading(false)
    }
  }

  const handleCancel = () => {
    if (window.confirm("작성 중인 내용이 저장되지 않습니다. 취소하시겠습니까?")) {
      navigate("/recruitment")
    }
  }

  if (!user) {
    return <div className="loading">로그인 정보를 불러오는 중입니다...</div>
  }

  if (loading) {
    return <div className="loading">로딩 중...</div>
  }

  if (!userProfiles.length) {
    return (
      <div className="recruitment-create-page">
        <div className="no-profiles">
          <h2>프로필이 없습니다</h2>
          <p>모집글을 작성하려면 먼저 프로필을 등록해주세요.</p>
          <button className="btn btn-primary" onClick={() => navigate("/mypage/profile-edit")}>
            프로필 등록하기
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="recruitment-create-page">
      <div className="page-header">
        <h1>모집글 작성</h1>
      </div>

      <div className="recruitment-form-container">
        <form onSubmit={handleSubmit} className="recruitment-form">
          <div className="form-group">
            <label htmlFor="profileId">프로필 선택</label>
            <select
              id="profileId"
              name="profileId"
              value={formData.profileId}
              onChange={handleChange}
              className="form-control"
              required
            >
              <option value="">프로필을 선택해주세요</option>
              {userProfiles.map((profile) => (
                <option key={profile.id} value={profile.id}>
                  {profile.name} ({profile.type === "IP" ? "IP 캐릭터" : "매장"})
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label htmlFor="title">제목</label>
            <input
              type="text"
              id="title"
              name="title"
              value={formData.title}
              onChange={handleChange}
              className="form-control"
              placeholder="모집글 제목을 입력해주세요"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="description">내용</label>
            <textarea
              id="description"
              name="description"
              value={formData.description}
              onChange={handleChange}
              className="form-control"
              rows="10"
              placeholder="모집 내용을 상세히 작성해주세요"
              required
            ></textarea>
          </div>

          <div className="form-group">
            <label htmlFor="deadline">마감일</label>
            <input
              type="date"
              id="deadline"
              name="deadline"
              value={formData.deadline}
              onChange={handleChange}
              className="form-control"
              min={new Date().toISOString().split("T")[0]}
              required
            />
          </div>

          <div className="form-actions">
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? "등록 중..." : "등록하기"}
            </button>
            <button type="button" className="btn btn-secondary" onClick={handleCancel} disabled={loading}>
              취소
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default RecruitmentCreatePage