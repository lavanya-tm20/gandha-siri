import { Outlet, Link, useLocation } from 'react-router-dom';
import { Home, TreeDeciduous, Map as MapIcon, LineChart, ShieldAlert, BookOpen, User, LogOut } from 'lucide-react';
import { cn } from '../lib/utils';
import { auth } from '../lib/firebase';
import { signOut } from 'firebase/auth';

const navItems = [
  { icon: Home, label: 'Dashboard', path: '/' },
  { icon: TreeDeciduous, label: 'My Trees', path: '/trees' },
  { icon: MapIcon, label: 'Tree Map', path: '/map' },
  { icon: ShieldAlert, label: 'Security Center', path: '/security' },
  { icon: LineChart, label: 'Growth', path: '/growth' },
  { icon: BookOpen, label: 'Legal Guide', path: '/legal' },
  { icon: User, label: 'Settings', path: '/settings' },
];

export default function Layout() {
  const location = useLocation();

  const handleLogout = () => {
    signOut(auth);
  };

  return (
    <div className="min-h-screen flex flex-col md:flex-row bg-sandal-light">
      {/* Desktop Sidebar */}
      <aside className="hidden md:flex flex-col w-64 bg-wood-primary text-white p-6 sticky top-0 h-screen">
        <div className="flex items-center gap-3 mb-12">
          <div className="w-10 h-10 bg-white/20 rounded-xl flex items-center justify-center">
            <TreeDeciduous className="w-6 h-6" />
          </div>
          <span className="font-bold text-xl tracking-tight">Gandha-Siri</span>
        </div>

        <nav className="flex-1 space-y-2">
          {navItems.map((item) => (
            <Link
              key={item.path}
              to={item.path}
              className={cn(
                "flex items-center gap-3 px-4 py-3 rounded-xl transition-all duration-200",
                location.pathname === item.path 
                  ? "bg-white text-wood-primary shadow-lg shadow-black/10" 
                  : "hover:bg-white/10 text-white/70"
              )}
            >
              <item.icon className="w-5 h-5" />
              <span className="font-medium">{item.label}</span>
            </Link>
          ))}
        </nav>

        <div className="mt-auto pt-6 border-t border-white/10 space-y-2">
          <button 
            onClick={handleLogout}
            className="w-full flex items-center gap-3 px-4 py-3 rounded-xl text-white/70 hover:bg-white/10 transition-colors"
          >
            <LogOut className="w-5 h-5" />
            <span className="font-medium">Logout</span>
          </button>
        </div>
      </aside>

      {/* Mobile Header */}
      <header className="md:hidden flex items-center justify-between px-6 py-4 bg-wood-primary text-white sticky top-0 z-50">
        <div className="flex items-center gap-2">
          <TreeDeciduous className="w-6 h-6" />
          <span className="font-bold text-lg">Gandha-Siri</span>
        </div>
        <button onClick={handleLogout} className="p-2">
          <LogOut className="w-5 h-5" />
        </button>
      </header>

      {/* Main Content */}
      <main className="flex-1 px-4 py-6 md:px-12 md:py-10 pb-24 md:pb-10">
        <Outlet />
      </main>

      {/* Mobile Bottom Nav */}
      <nav className="md:hidden fixed bottom-0 left-0 right-0 h-16 bg-white border-t border-sandal-base/20 flex items-center justify-around px-2 z-50">
        {navItems.map((item) => (
          <Link
            key={item.path}
            to={item.path}
            className={cn(
              "flex flex-col items-center gap-1 transition-colors",
              location.pathname === item.path ? "text-wood-primary" : "text-sandal-base"
            )}
          >
            <item.icon className="w-5 h-5" />
            <span className="text-[10px] font-medium uppercase tracking-wider">{item.label}</span>
          </Link>
        ))}
      </nav>
    </div>
  );
}
