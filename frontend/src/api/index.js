"use client";

import React, { useState, useEffect } from "react";
import axios from "axios";
import {
  getAccessToken,
  getRefreshToken,
  setTokens,
  setAccessToken,
  clearTokens,
  setGovAccessToken,
  getGovAccessToken,
} from "../utils/storage";

const BASE_URL = "http://localhost:8080";

const consumerKey = "f7ddf21748374d63a9f7";
const consumerSecret = "9111abed50bb40c9bba3";

// 브라우저 환경인지 확인
const isBrowser = typeof window !== "undefined";

export const api = axios.create({
  baseURL: BASE_URL,
});

// 브라우저 환경에서만 토큰 처리
if (isBrowser) {
  api.interceptors.request.use((config) => {
    const token = getAccessToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  });

  // Add a response interceptor to handle common errors
  api.interceptors.response.use(
    (response) => response,
    async (error) => {
      const original = error.config;
      // 응답 코드가 401이면 엑세스 토큰 재발급
      if (error.response?.status === 401 && !original._retry) {
        original._retry = true;
        const refreshToken = getRefreshToken();
        // 리프레시 토큰이 없으면 로그아웃 처리
        if (!refreshToken) {
          clearTokens();
          window.location.href = "/login";
          return Promise.reject(error);
        }
        // 재발급 요청
        const { data } = await axios.post(`${BASE_URL}/api/tokens/refresh`, {
          refreshToken,
        });
        setAccessToken({
          accessToken: data.accessToken,
        });
        // 교체된 토큰으로 원 요청 재시도
        original.headers.Authorization = `Bearer ${data.accessToken}`;
        return api(original);
      }

      // 응답 코드가 410이면 리프레시 토큰 만료로 로그아웃 처리
      if (error.response?.status === 410) {
        clearTokens();
        window.location.href = "/login";
      }
      return Promise.reject(error);
    }
  );
}

export const apiClient = axios.create({
  baseURL: "https://sgisapi.kostat.go.kr/OpenAPI3",
});

apiClient.interceptors.response.use(
  async (response) => {
    // 응답이 200이어도 내부 errCd가 -401이면 예외 처리
    if (response.data?.errCd === -401) {
      try {
        const authRes = await apiClient.get("/auth/authentication.json", {
          params: {
            consumer_key: consumerKey,
            consumer_secret: consumerSecret,
          },
        });
        const newToken = authRes.data.result.accessToken;
        setGovAccessToken(newToken);

        window.location.href = "/";
      } catch (e) {
        return Promise.reject(e);
      }
    }

    return response;
  },
  (error) => {
    // 일반적인 HTTP 오류 처리
    return Promise.reject(error);
  }
);

// Token API
export const tokenAPI = {
  logout: (refreshToken) => api.post("/api/tokens/logout", refreshToken),
};

// User API
export const userAPI = {
  signup: (userData) => api.patch("/api/users/signup", userData),
  getMyUserInfo: () => api.get("/api/users"),
  getUserInfo: (userId) => api.get(`/api/users/${userId}`),
  updateUserInfo: (data) => api.patch(`/api/users`, data),
  deleteAccount: () => api.delete(`/api/users`),
};

// Profile API
export const profileAPI = {
  getUserProfiles: (userId) => api.get(`/api/profiles/user/${userId}`),
  createProfile: (data) => api.post("/api/profiles", data),
  getProfile: (profileId) => api.get(`/api/profiles/${profileId}`),
  updateProfile: (profileId, data) =>
    api.patch(`/api/profiles/${profileId}`, data),
  deleteProfile: (profileId) => api.delete(`/api/profiles/${profileId}`),
  getIPProfiles: (params) => api.get("/api/profiles/ip", { params }),
  getStoreProfiles: (params) => api.get("/api/profiles/store", { params }),
  searchProfiles: (params) => api.get(`/api/profiles/search?${params}`),
};

// Recruitment API
export const recruitmentAPI = {
  createRecruitment: (data) => api.post("/api/recruitments", data),
  getRecruitment: (id) => api.get(`/api/recruitments/${id}`),
  updateRecruitment: (id, data) => api.patch(`/api/recruitments/${id}`, data),
  deleteRecruitment: (id) => api.delete(`/api/recruitments/${id}`),
  getRecruitments: (params) => api.get("/api/recruitments", { params }),
  getUserRecruitments: (userId) => api.get(`/api/recruitments/users/${userId}`),
  getProfileRecruitments: (profileId) =>
    api.get(`/api/recruitments/profiles/${profileId}`),
};

// Application API
export const applicationAPI = {
  applyToRecruitment: (recruitmentId, data) =>
    api.post(`/api/applications/${recruitmentId}`, data),
  getSentApplications: (params) =>
    api.get("/api/applications/sent", { params }),
  getReceivedApplications: (params) =>
    api.get("/api/applications/received", { params }),
  acceptApplication: (applicationId) =>
    api.patch(`/api/applications/accept/${applicationId}`),
};

// Admin API
export const adminAPI = {
  createTag: (data) => api.post("/admin/tags", data),
  deleteTag: (tagId) => api.delete(`/admin/tags/${tagId}`),
  getAllUsers: () => api.get("/admin/users"),
  deleteUser: (userId) => api.delete(`/admin/users/${userId}`),
};

// Tag API
export const tagAPI = {
  getAllTags: () => api.get("/api/tags"),
  getIPTags: () => api.get("/api/tags?type=IP"),
  getStoreTags: () => api.get("/api/tags?type=STORE"),
};

// // Region API
// export const regionAPI = {
//   getAddress: async (cd) => {
//     const token = await getGovAccessToken();
//     const res = await apiClient.get(
//       `/addr/stage.json?accessToken=${token}${cd ? `&cd=${cd}` : ""}`
//     );
//     return res;
//   },
// };

export const regionAPI = {
  // 기존: 법정동 단계별 공통 엔드포인트
  getAddress: async (cd) => {
    const token = await getGovAccessToken();
    return apiClient.get(
      `/addr/stage.json?accessToken=${token}${cd ? `&cd=${cd}` : ""}`
    );
  },

  // 1) 시/도 목록만: cd 없이 호출
  getProvinces() {
    return this.getAddress();
  },

  // 2) 시/군/구: 상위 코드 넘겨서 호출
  getDistricts(cd) {
    return this.getAddress(cd);
  },

  // 3) 읍/면/동: 상위 코드 넘겨서 호출
  getNeighborhoods(cd) {
    return this.getAddress(cd);
  },
};

export const mailAPI = {
  sendRequestMail: (profileId) => api.post(`/api/mail/${profileId}`),
};
