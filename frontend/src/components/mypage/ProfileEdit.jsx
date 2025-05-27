"use client"

import { useState, useEffect } from "react"
import { getUserId, getRole } from "../../utils/storage"
import { profileAPI, tagAPI, regionAPI } from "../../api"
import ProfileCreateModal from "./ProfileCreateModal"
import SingleRegionSelector from "../SingleRegionSelector"
import "./ProfileEdit.css"



const ProfileEdit = () => {
  const userId = parseInt(getUserId(), 10)
  const role = getRole()
  const [provinces, setProvinces]       = useState([])     // 시/도
  const [districts, setDistricts]       = useState([])     // 시/군/구
  const [neighborhoods, setNeighborhoods] = useState([])    // 읍/면/동

  const [selProvince, setSelProvince]       = useState(null)
  const [selDistrict, setSelDistrict]       = useState(null)
  const [selNeighborhood, setSelNeighborhood] = useState(null)
  if (!userId || !role) return null

  const isIP    = role === "ROLE_IP"
  const isStore = role === "ROLE_STORE"

  const [profiles, setProfiles] = useState([])
  const [allTags, setAllTags] = useState([])
  const [regions, setRegions] = useState([])
  const [editingProfile, setEditingProfile] = useState(null)
  const [showCreateModal, setShowCreateModal] = useState(false)
  const [loading, setLoading] = useState(true)

  const initialForm = {
    name: "",
    description: "",
    tags: [],
    status: "true",
    // images
    profileImageFile: null,
    profileImagePreview: null,
    thumbnailImageFile: null,
    thumbnailImagePreview: null,
    extraImageFiles: [],
    extraImagePreviews: [],
    // address for STORE
    addressCode: "",
    address: "",
  }
  const [formData, setFormData] = useState(initialForm)

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [profilesRes, tagsRes, provincesRes] = await Promise.all([
          profileAPI.getUserProfiles(userId),
          tagAPI.getAllTags(),
         
        ])
        setProfiles(profilesRes.data || [])
        setAllTags(tagsRes.data)
        
         // **시/도 목록만 미리 가져오기**
         const provRes = await regionAPI.getAddress()
         setProvinces(provRes.data.result || [])

      } catch (err) {
        console.error("Error fetching data:", err)
      } finally {
        setLoading(false)
      }
    }
    fetchData()
  }, [userId])

  const filteredTags = allTags.filter(tag => {
    const t = tag.tagType ?? tag.type
    if (isIP)    return t === "IP"
    if (isStore) return t === "STORE"
    return false
  })
  // selProvince 변경 시
useEffect(() => {
  if (!selProvince) {
    setDistricts([])
    return
  }
  regionAPI.getAddress(selProvince.cd)
    .then(res => {
      setDistricts(res.data.result || [])
      setSelDistrict(null)
      setNeighborhoods([])
    })
    .catch(console.error)
}, [selProvince])

// selDistrict 변경 시
useEffect(() => {
  if (!selDistrict) {
    setNeighborhoods([])
    return
  }
  regionAPI.getAddress(selDistrict.cd)
    .then(res => {
      setNeighborhoods(res.data.result || [])
      setSelNeighborhood(null)
    })
    .catch(console.error)
}, [selDistrict])

  const resetForm = () => setFormData(initialForm)

  const handleEditClick = (profile) => {
    setEditingProfile(profile)
    setFormData({
      name: profile.name,
      description: profile.description,
      tags: profile.tags.map(t => t.id),
      status: profile.status.toString(),
      profileImageFile: null,
      profileImagePreview: profile.profileImageUrl // 파일명만 들고있던 기존 상태 -> 파일명이 있으면 로컬8080 전체url 주고 없으면 널
        ? `http://localhost:8080/api/files/images/${profile.profileImageUrl}`
        : null,

      thumbnailImageFile: null,
      thumbnailImagePreview: profile.thumbnailImageUrl
        ? `http://localhost:8080/api/files/images/${profile.thumbnailImageUrl}`
        : null,

      extraImageFiles: [],
      extraImagePreviews: Array.isArray(profile.extraImageUrls)
        ? profile.extraImageUrls.map(filename =>
          `http://localhost:8080/api/files/images/${filename}`
        )
        : [],

      addressCode: profile.addressCode || "",
      address: profile.address || "",
    })
  }

  const handleCreateClick = () => setShowCreateModal(true)

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
  }

  const handleTagChange = (e) => {
    const selected = Array.from(e.target.selectedOptions).map(o => o.value)
    setFormData(prev => ({ ...prev, tags: selected }))
  }

  const handleProfileImageChange = (e) => {
    const file = e.target.files[0]
    if (file) setFormData(prev => ({
      ...prev,
      profileImageFile: file,
      profileImagePreview: URL.createObjectURL(file)
    }))
  }
  const handleThumbnailChange = (e) => {
    const file = e.target.files[0]
    if (file) setFormData(prev => ({
      ...prev,
      thumbnailImageFile: file,
      thumbnailImagePreview: URL.createObjectURL(file)
    }))
  }
  const handleExtraImagesChange = (e) => {
    const files = Array.from(e.target.files)
    const previews = files.map(f => URL.createObjectURL(f))
    setFormData(prev => ({
      ...prev,
      extraImageFiles: files,
      extraImagePreviews: previews
    }))
  }

  const handleRegionSelect = (selection) => {
    setFormData(prev => ({
      ...prev,
      addressCode: selection.code,
      address: selection.fullAddress
    }))
  }

 const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      setLoading(true);

      // 1) ProfileRequestDto 에 맞춘 JSON 파트 생성
      const profileRequest = {
        name:        formData.name,
        description: formData.description,
        tagIds:      formData.tags,
        status:      formData.status === "true",
        type:        isIP ? "IP" : "STORE",
        // STORE일 때만 addressId 필드로 전달
        addressCode:   !isIP ? formData.addressCode : undefined,
        address:     !isIP ? formData.address : undefined,
      };

      // 2) FormData 준비
      const data = new FormData();
      // 가장 먼저 profileRequest 파트로 붙입니다 (application/json)
      data.append(
        "profileRequest",
        new Blob([JSON.stringify(profileRequest)], { type: "application/json" })
      );

      // 3) 이미지 파일 파트들
      if (formData.profileImageFile) {
        data.append("profileImage", formData.profileImageFile);
      }
      if (formData.thumbnailImageFile) {
        data.append("thumbnailImage", formData.thumbnailImageFile);
      }
      formData.extraImageFiles.forEach((file) => {
        data.append("extraImages", file);
      });

      // 4) 업데이트 API 호출
      await profileAPI.updateProfile(editingProfile.id, data);

      // 5) 목록 새로고침
      const refreshed = await profileAPI.getUserProfiles(userId);
      setProfiles(refreshed.data || []);
      setEditingProfile(null);
      resetForm();
    } catch (err) {
      console.error("Error saving profile:", err);
      alert("프로필 저장에 실패했습니다.");
    } finally {
      setLoading(false);
    }
  }


  const handleCancel = () => {
    setEditingProfile(null)
    resetForm()
  }

  const handleDeleteProfile = async (id) => {
    if (!window.confirm("정말로 프로필을 삭제하시겠습니까?")) return
    try {
      setLoading(true)
      await profileAPI.deleteProfile(id)
      const refreshed = await profileAPI.getUserProfiles(userId)
      setProfiles(refreshed.data || [])
      alert("프로필이 삭제되었습니다.")
    } catch (err) {
      console.error(err)
      alert("프로필 삭제에 실패했습니다.")
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="profile-edit">
      <div className="section-header">
        <h2>프로필 편집</h2>
        <button className="btn btn-primary" onClick={handleCreateClick}>
          {isIP ? "새 IP 캐릭터 등록" : "매장 등록"}
        </button>
      </div>

      {editingProfile ? (
        <form onSubmit={handleSubmit} className="profile-form">
          <h3>프로필 수정</h3>

          {/* PROFILE Image */}
          <div className="form-group">
            <label>프로필 이미지 (PROFILE)</label>
            <div className="image-upload">
              <div className="image-preview">
                <img
                  src={formData.profileImagePreview }
                  alt=""
                  onError={e => { e.currentTarget.src = "" }} 
                  style={{ backgroundColor: "#f0f0f0" }}
                />
              </div>
              <input type="file" accept="image/*" onChange={handleProfileImageChange} />
            </div>
          </div>

          {/* THUMBNAIL Image */}
          <div className="form-group">
            <label>썸네일 이미지 (THUMBNAIL)</label>
            <div className="image-upload">
              <div className="image-preview">
                 {formData.thumbnailImagePreview ? (
                  <img
                    src={formData.thumbnailImagePreview}
                    alt="썸네일 미리보기"
                    onError={e => {
                      e.currentTarget.onerror = null;
                      e.currentTarget.src = "/placeholder-thumbnail.png";
                    }}
                    />
                  ) : (
                    <div className="empty-preview">썸네일이 없습니다</div>
                  )}

              </div>
              <input type="file" accept="image/*" onChange={handleThumbnailChange} />
              </div>
          </div>

          {/* EXTRA Images */}
          <div className="form-group">
            <label>추가 이미지 (EXTRA)</label>
            <div className="image-upload multiple">
              <div className="image-previews">
                {formData.extraImagePreviews.map((src, idx) => (
                  <img key={idx} src={src} alt={`extra-${idx}`} />
                ))}
              </div>
              <input type="file" accept="image/*" multiple onChange={handleExtraImagesChange} />
            </div>
          </div>

          {/* Name, Description */}
          <div className="form-group">
            <label htmlFor="name">이름</label>
            <input id="name" name="name" value={formData.name} onChange={handleChange} className="form-control" required />
          </div>
          <div className="form-group">
            <label htmlFor="description">소개</label>
            <textarea id="description" name="description" value={formData.description} onChange={handleChange} className="form-control" rows={4} required />
          </div>

          {/* Address (STORE only) */}
          {!isIP && (
          <div className="form-group">
            <label>주소 선택</label>
            <div className="region-dropdowns">
              <select
                value={selProvince?.cd || ""}
                onChange={e => {
                  const prov = provinces.find(p => p.cd === e.target.value);
                  setSelProvince(prov || null);
                  setFormData(prev => ({
                    ...prev,
                    addressCode: prov?.cd || "",
                    address: prov ? prov.addr_name : ""
                  }));
                }}
              >
                <option value="">시/도 선택</option>
                {provinces.map(p => (
                  <option key={p.cd} value={p.cd}>
                    {p.addr_name}
                  </option>
                ))}
              </select>

              <select
                disabled={!selProvince}
                value={selDistrict?.cd || ""}
                onChange={e => {
                  const dist = districts.find(d => d.cd === e.target.value);
                  setSelDistrict(dist || null);
                  setFormData(prev => ({
                    ...prev,
                    addressCode: dist?.cd || prev.addressCode,
                    address: dist
                      ? `${selProvince.addr_name} ${dist.addr_name}`
                      : prev.address
                  }));
                }}
              >
                <option value="">시/군/구 선택</option>
                {districts.map(d => (
                  <option key={d.cd} value={d.cd}>
                    {d.addr_name}
                  </option>
                ))}
              </select>

              <select
                disabled={!selDistrict}
                value={selNeighborhood?.cd || ""}
                onChange={e => {
                  const hood = neighborhoods.find(n => n.cd === e.target.value);
                  setSelNeighborhood(hood || null);
                  setFormData(prev => ({
                    ...prev,
                    addressCode: hood?.cd || prev.addressCode,
                    address: hood
                      ? `${selProvince.addr_name} ${selDistrict.addr_name} ${hood.addr_name}`
                      : prev.address
                  }));
                }}
              >
                <option value="">읍/면/동 선택</option>
                {neighborhoods.map(n => (
                  <option key={n.cd} value={n.cd}>
                    {n.addr_name}
                  </option>
                ))}
              </select>
            </div>
            {formData.address && (
              <p className="selected-address">선택된 주소: {formData.address}</p>
            )}
          </div>
          )}


          {/* Tags */}
          <div className="form-group">
            <label>태그 (여러 개 선택 가능)</label>
            <select multiple value={formData.tags} onChange={handleTagChange} className="form-control" >
              {filteredTags.map(tag => (
                <option key={tag.id} value={tag.id}>{tag.name}</option>
              ))}
            </select>
            <small>Ctrl 키를 누르고 선택하세요.</small>
          </div>

          {/* Status */}
          <div className="form-group">
            <label>상태</label>
            <select name="status" value={formData.status} onChange={handleChange} className="form-control">
              <option value="true">활성</option>
              <option value="false">비활성</option>
            </select>
          </div>

          {/* Actions */}
          <div className="form-actions">
            <button type="submit" className="btn btn-primary">저장</button>
            <button type="button" className="btn btn-secondary" onClick={handleCancel}>취소</button>
          </div>
        </form>
      ) : (
        <div className="profiles-list">
          {profiles.length === 0 ? (
            <div className="no-profiles">
              <p>등록된 프로필이 없습니다.</p>
              <button className="btn btn-primary" onClick={handleCreateClick}>
                {isIP ? "새 IP 캐릭터 등록" : "매장 등록"}
              </button>
            </div>
          ) : (
            <div className="profiles-grid">
              {profiles.map(profile => (
                <div key={profile.id} className="profile-card">
                  <div className="profile-image">
                  <img
                    src={profile.profileImageUrl
                      ? `http://localhost:8080/api/files/images/${profile.profileImageUrl}`
                      : "/placeholder-profile.png"}
                    alt=""
                    onError={e => {
                      e.currentTarget.onerror = null;    // 무한루프 방지
                      e.currentTarget.src = "/placeholder-profile.png";
                    }}
                    style={{ backgroundColor: "#f0f0f0" }}
                  />
                    <div className={`status-badge ${profile.status ? "active" : "inactive"}`}>
                      {profile.status ? "활성" : "비활성"}
                    </div>
                  </div>
                  <div className="profile-info">
                    <h3>{profile.name}</h3>
                    <p className="profile-description">{profile.description}</p>
                    <div className="profile-meta">
                      <span>생성일: {new Date(profile.createdAt).toLocaleDateString()}</span>
                      <span>콜라보 횟수: {profile.collaboCount || 0}</span>
                    </div>
                    <div className="profile-tags">
                      {profile.tags.map(tag => <span key={tag.id} className="tag">{tag.name}</span>)}
                    </div>
                    <div className="profile-actions">
                      <button className="btn btn-primary" onClick={() => handleEditClick(profile)}>수정</button>
                      <button className="btn btn-danger" onClick={() => handleDeleteProfile(profile.id)}>삭제</button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {showCreateModal && (
        <ProfileCreateModal
          onClose={() => setShowCreateModal(false)}
          onProfileCreated={async () => {
            const res = await profileAPI.getUserProfiles(userId)
            setProfiles(res.data || [])
          }}
          tags={filteredTags}
          regions={regions}
          isIP={isIP}
        />
      )}
    </div>
  )
}

export default ProfileEdit
