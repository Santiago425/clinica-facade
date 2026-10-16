import { createContext, useContext, useState } from 'react'

const AuthContext = createContext(null)
const STORAGE_KEY = 'clinic-patient'

function readStoredPatient() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export function AuthProvider({ children }) {
  const [patient, setPatient] = useState(readStoredPatient)

  const signIn = (profile) => {
    setPatient(profile)
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(profile))
    } catch {
      return
    }
  }

  const signOut = () => {
    setPatient(null)
    try {
      localStorage.removeItem(STORAGE_KEY)
    } catch {
      return
    }
  }

  return <AuthContext.Provider value={{ patient, signIn, signOut }}>{children}</AuthContext.Provider>
}

export function useAuth() {
  return useContext(AuthContext)
}
