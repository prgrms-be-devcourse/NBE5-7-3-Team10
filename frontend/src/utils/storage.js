// src/utils/storage.js

import { apiClient } from "../api";

const TOKEN_KEY = {
  ACCESS: "access_token",
  REFRESH: "refresh_token",
  USER_ID: "userId",
  NICKNAME: "nickname",
  ROLE: "role",
  GOVACCESS: "gov_access_token",
};

const consumerKey = "963958cd0e98427cbe84";
const consumerSecret = "ddbd3f5bf9d0492583cf";

export const getAccessToken = () => localStorage.getItem(TOKEN_KEY.ACCESS);

export const getRefreshToken = () => localStorage.getItem(TOKEN_KEY.REFRESH);

export const getUserId = () => localStorage.getItem(TOKEN_KEY.USER_ID);

export const getNickname = () => localStorage.getItem(TOKEN_KEY.NICKNAME);

export const getRole = () => localStorage.getItem(TOKEN_KEY.ROLE);

export const getGovAccessToken = async () => {
  const token = localStorage.getItem(TOKEN_KEY.GOVACCESS);
  if (token !== undefined && token !== null) {
    return token;
  }

  try {
    // API 호출
    const response = await apiClient.get("/auth/authentication.json", {
      params: {
        consumer_key: consumerKey,
        consumer_secret: consumerSecret,
      },
    });

    const accessToken = response.data.result.accessToken;

    // 토큰 저장
    localStorage.setItem(TOKEN_KEY.GOVACCESS, accessToken);
    return accessToken;
  } catch (error) {
    // 에러 로깅 또는 사용자 알림 처리
    alert("Token 발급 중 오류 발생:", error);
    throw error;
  }
};

export const getUserInfo = () => {
  const accessToken = localStorage.getItem(TOKEN_KEY.ROLE);
  const refreshToken = localStorage.getItem(TOKEN_KEY.REFRESH);
  const userId = localStorage.getItem(TOKEN_KEY.USER_ID);
  const nickname = localStorage.getItem(TOKEN_KEY.NICKNAME);
  const role = localStorage.getItem(TOKEN_KEY.ROLE);

  return { accessToken, refreshToken, userId, nickname, role };
};

export const isSignedIn = () => {
  const refreshToken = localStorage.getItem(TOKEN_KEY.REFRESH);
  if (refreshToken !== null) {
    return true;
  }

  return false;
};

export const isSignedUp = () => {
  const role = localStorage.getItem(TOKEN_KEY.ROLE);
  if (role !== null && role !== "ROLE_TMP") {
    return true;
  }

  return false;
};

export const setTokens = ({
  accessToken,
  refreshToken,
  userId,
  nickname,
  role,
}) => {
  localStorage.setItem(TOKEN_KEY.ACCESS, accessToken);
  localStorage.setItem(TOKEN_KEY.REFRESH, refreshToken);
  localStorage.setItem(TOKEN_KEY.USER_ID, userId);
  localStorage.setItem(TOKEN_KEY.NICKNAME, nickname);
  localStorage.setItem(TOKEN_KEY.ROLE, role);
};

export const setGovAccessToken = (govAccessToken) => {
  localStorage.setItem(TOKEN_KEY.GOVACCESS, govAccessToken);
};

export const setAccessToken = (accessToken) => {
  localStorage.setItem(TOKEN_KEY.ACCESS, accessToken);
};

export const setNickname = (nickname) => {
  localStorage.setItem(TOKEN_KEY.NICKNAME, nickname);
};

export const setRole = (role) => {
  localStorage.setItem(TOKEN_KEY.ROLE, role);
};

export const clearTokens = () => {
  localStorage.removeItem(TOKEN_KEY.ACCESS);
  localStorage.removeItem(TOKEN_KEY.REFRESH);
  localStorage.removeItem(TOKEN_KEY.USER_ID);
  localStorage.removeItem(TOKEN_KEY.NICKNAME);
  localStorage.removeItem(TOKEN_KEY.ROLE);
};
