import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import PrivateRoute from './components/common/PrivateRoute';
import BidListPage from './pages/BidListPage';
import BidDetailPage from './pages/BidDetailPage';
import CompanyProfilePage from './pages/CompanyProfilePage';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />
        <Route
          path="/"
          element={
            <PrivateRoute>
              <BidListPage />
            </PrivateRoute>
          }
        />
        <Route
          path="/company/profile"
          element={
            <PrivateRoute>
              <CompanyProfilePage />
            </PrivateRoute>
          }
        />
        <Route
          path="/bids/:bidId"
          element={
            <PrivateRoute>
              <BidDetailPage />
            </PrivateRoute>
          }
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
