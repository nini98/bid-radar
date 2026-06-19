import { BrowserRouter, Routes, Route } from 'react-router-dom';
import BidListPage from './pages/BidListPage';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<BidListPage />} />
      </Routes>
    </BrowserRouter>
  );
}
