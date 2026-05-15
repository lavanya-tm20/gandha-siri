import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useEffect, useState, createContext, useContext } from 'react';
import { onAuthStateChanged, User } from 'firebase/auth';
import { auth } from './lib/firebase';
import { motion, AnimatePresence } from 'motion/react';
import { StatusBar, Style } from '@capacitor/status-bar';
import { SplashScreen } from '@capacitor/splash-screen';

// Pages
import Dashboard from './pages/Dashboard';
import Login from './pages/Login';
import AddTree from './pages/AddTree';
import TreeList from './pages/TreeList';
import TreeDetail from './pages/TreeDetail';
import GrowthTracker from './pages/GrowthTracker';
import MapScreen from './pages/MapScreen';
import LegalGuide from './pages/LegalGuide';
import Settings from './pages/Settings';
import SecurityAlerts from './pages/SecurityAlerts';

// Components
import Layout from './components/Layout';

interface AuthContextType {
  user: User | null;
  loading: boolean;
}

const AuthContext = createContext<AuthContextType>({ user: null, loading: true });
export const useAuth = () => useContext(AuthContext);

export default function App() {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Initialize Capacitor features for native app experience
    const initApp = async () => {
      try {
        await StatusBar.setStyle({ style: Style.Light });
        await SplashScreen.hide();
      } catch (e) {
        // Fallback for web view or if Capacitor is not present
        console.info('Native features not initialized:', (e as Error).message);
      }
    };
    initApp();

    const unsubscribe = onAuthStateChanged(auth, (user) => {
      setUser(user);
      setLoading(false);
    });
    return unsubscribe;
  }, []);

  if (loading) {
    return (
      <div className="h-screen w-screen flex items-center justify-center bg-sandal-light">
        <motion.div
          animate={{ scale: [1, 1.2, 1], rotate: [0, 180, 360] }}
          transition={{ duration: 2, repeat: Infinity }}
          className="w-12 h-12 border-4 border-wood-primary border-t-transparent rounded-full"
        />
      </div>
    );
  }

  return (
    <AuthContext.Provider value={{ user, loading }}>
      <BrowserRouter>
        <AnimatePresence mode="wait">
          <Routes>
            <Route path="/login" element={!user ? <Login /> : <Navigate to="/" />} />
            
            <Route element={user ? <Layout /> : <Navigate to="/login" />}>
              <Route path="/" element={<Dashboard />} />
              <Route path="/add-tree" element={<AddTree />} />
              <Route path="/trees" element={<TreeList />} />
              <Route path="/trees/:id" element={<TreeDetail />} />
              <Route path="/growth" element={<GrowthTracker />} />
              <Route path="/map" element={<MapScreen />} />
              <Route path="/legal" element={<LegalGuide />} />
              <Route path="/settings" element={<Settings />} />
              <Route path="/security" element={<SecurityAlerts />} />
            </Route>
          </Routes>
        </AnimatePresence>
      </BrowserRouter>
    </AuthContext.Provider>
  );
}
