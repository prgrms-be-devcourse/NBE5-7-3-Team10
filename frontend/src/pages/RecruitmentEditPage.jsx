"use client"

import { useState, useEffect, useContext } from "react"
import { useNavigate, useParams } from "react-router-dom"
import { recruitmentAPI, profileAPI } from "../api"
import { getUserInfo } from "../utils/storage"
import "./RecruitmentCreatePage.css" // 같은 스타일 사용

const RecruitmentEditPage = () => {
  const { id } = useParams()
  const  user  =getUserInfo()
  const navigate = useNavigate()
  const [userProfiles, setUserProfiles] = useState([])
  const [loading, setLoading] = useState(true)
  const [formData, setFormData] = useState({
    profileId: "",
    title: "",
    description: "",
    deadline: "",
    status: "RECRUITING" 
    
  })

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true)

        // 모집 공고 정보 가져오기
        const recruitmentResponse = await recruitmentAPI.getRecruitment(id)
        const recruitment = recruitmentResponse.data

        // 사용자 프로필 가져오기
        const profilesResponse = await profileAPI.getUserProfiles(user.userId)
        setUserProfiles(profilesResponse.data)

        // 폼 데이터 설정
        setFormData({
          profileId: recruitment.profile.id,
          title: recruitment.title,
          description: recruitment.description,
          deadline: new Date(recruitment.deadline).toISOString().split("T")[0],
          status: recruitment.status
        })
      } catch (error) {
        console.error("Error fetching data:", error)
        alert("모집 공고 정보를 불러오는데 실패했습니다.")
        navigate("/mypage/my-recruitments")
      } finally {
        setLoading(false)
      }
    }

    fetchData()
  }, [id, user.userId, navigate])

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

      await recruitmentAPI.updateRecruitment(id,{ ...formData,  deadline: `${formData.deadline}T00:00:00`})

      alert("모집 공고가 수정되었습니다.")
      navigate("/mypage/my-recruitments")
    } catch (error) {
      console.error("Error updating recruitment:", error)
      alert("모집 공고 수정에 실패했습니다.")
    } finally {
      setLoading(false)
    }
  }

  const handleCancel = () => {
    if (window.confirm("수정 중인 내용이 저장되지 않습니다. 취소하시겠습니까?")) {
      navigate("/mypage/my-recruitments")
    }
  }

  if (loading) {
    return <div className="loading">로딩 중...</div>
  }

  return (
    <div className="recruitment-create-page">
      <div className="page-header">
        <h1>모집 공고 수정</h1>
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

          <div className="form-group">
            <label htmlFor="status">상태</label>
            <select
              id="status"
              name="status"
              value={formData.status}
              onChange={handleChange}
              className="form-control"
              required
            >
              <option value="RECRUITING">모집중</option>
              <option value="COMPLETED">매칭완료</option>
            </select>
          </div>

          <div className="form-actions">
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? "수정 중..." : "수정하기"}
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

export default RecruitmentEditPage
