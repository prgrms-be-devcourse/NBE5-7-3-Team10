"use client"

// src/context/AuthContext.js
import { createContext, useState, useEffect } from 'react';
import { clearTokens, setTokens, isSignedUp, getUserInfo, isSignedIn, getRefreshToken } from '../utils/storage';
import { tokenAPI } from '../api'

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const login = ({ accessToken, refreshToken, userId, nickname, role }) => {
    setTokens({ accessToken, refreshToken, userId, nickname, role });

    if(!isSignedUp()) {
      window.location.href = '/signup';
      return;
    }

    window.location.href = '/';
  };

  const logout = async () => {
    const refreshToken = getRefreshToken();
    console.log(refreshToken);
    await tokenAPI.logout(refreshToken);
    clearTokens();
    window.location.href = '/login';
  };

  return (
    <AuthContext.Provider value={{ login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};
