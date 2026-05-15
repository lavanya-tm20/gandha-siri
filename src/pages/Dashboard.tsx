import React, { useState, useEffect } from 'react';
import { collection, query, where, getDocs, limit, orderBy, addDoc, serverTimestamp } from 'firebase/firestore';
import { db, auth } from '../lib/firebase';
import { motion } from 'motion/react';
import { Plus, Map, LineChart, ShieldAlert, BookOpen, ChevronRight, TreeDeciduous } from 'lucide-react';
import { Link } from 'react-router-dom';
import { useAuth } from '../App';
import { Tree } from '../types';
import { cn } from '../lib/utils';

export default function Dashboard() {
  const { user } = useAuth();
  const [stats, setStats] = useState({ totalTrees: 0, latestTrees: [] as Tree[] });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function fetchDashboardData() {
      if (!user) return;
      try {
        const q = query(collection(db, 'trees'), where('userId', '==', user.uid));
        const snap = await getDocs(q);
        const trees = snap.docs.map(doc => ({ id: doc.id, ...doc.data() } as Tree));
        
        setStats({
          totalTrees: trees.length,
          latestTrees: trees.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()).slice(0, 3)
        });
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    }
    fetchDashboardData();
  }, [user]);

  const [sendingAlert, setSendingAlert] = useState(false);

  const triggerAlert = async () => {
    if (!user) return;
    if (!confirm('Are you sure you want to trigger a SECURITY PANIC ALERT? This will notify your emergency contacts and local forest guard.')) return;
    
    setSendingAlert(true);
    try {
      await addDoc(collection(db, 'alerts'), {
        type: 'theft',
        userId: user.uid,
        status: 'pending',
        createdAt: serverTimestamp(),
        location: null // Could fetch location here if needed
      });
      alert('URGENT: Security alarm triggered. Local authorities have been notified via simulated SMS.');
    } catch (err) {
      console.error(err);
    } finally {
      setSendingAlert(false);
    }
  };

  const quickActions = [
    { title: 'Add Tree', icon: Plus, path: '/add-tree', color: 'bg-green-500' },
    { title: 'View Map', icon: Map, path: '/map', color: 'bg-blue-500' },
    { title: 'Growth Tracker', icon: LineChart, path: '/growth', color: 'bg-orange-500' },
    { title: 'Legal Guide', icon: BookOpen, path: '/legal', color: 'bg-purple-500' },
  ];

  return (
    <div className="space-y-8 max-w-4xl">
      <div className="flex flex-col md:flex-row md:items-end justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-sandal-dark">Welcome, {user?.displayName || 'Farmer'}</h1>
          <p className="text-sandal-base font-medium">Monitoring the health of your sandalwood forest.</p>
        </div>
        <Link 
          to="/add-tree"
          className="inline-flex items-center gap-2 wood-gradient text-white px-6 py-3 rounded-2xl shadow-lg shadow-wood-primary/20 font-bold"
        >
          <Plus className="w-5 h-5" />
          Add New Tree
        </Link>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <motion.div 
          initial={{ opacity: 0, scale: 0.95 }}
          animate={{ opacity: 1, scale: 1 }}
          className="glass-card p-6 flex items-center gap-5"
        >
          <div className="w-14 h-14 bg-wood-primary/10 rounded-2xl flex items-center justify-center text-wood-primary">
            <TreeDeciduous className="w-8 h-8" />
          </div>
          <div>
            <p className="text-xs font-bold text-sandal-base uppercase tracking-widest">Total Trees</p>
            <p className="text-3xl font-black text-sandal-dark">{stats.totalTrees}</p>
          </div>
        </motion.div>

        <motion.div 
          onClick={triggerAlert}
          initial={{ opacity: 0, scale: 0.95 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ delay: 0.1 }}
          className={cn(
            "glass-card p-6 flex items-center gap-5 overflow-hidden relative group cursor-pointer border-red-100",
            sendingAlert && "animate-pulse border-red-500"
          )}
        >
          <div className="w-14 h-14 bg-red-500/10 rounded-2xl flex items-center justify-center text-red-500">
            <ShieldAlert className={cn("w-8 h-8", sendingAlert && "animate-bounce")} />
          </div>
          <div>
            <p className="text-xs font-bold text-sandal-base uppercase tracking-widest">Security Alert</p>
            <p className="text-lg font-bold text-red-600 underline decoration-2 underline-offset-4">{sendingAlert ? 'Triggering...' : 'Panic Button'}</p>
          </div>
          <Link to="/security" className="absolute right-4 top-1/2 -translate-y-1/2 p-2 bg-sandal-light rounded-lg hover:bg-white transition-colors" onClick={(e) => e.stopPropagation()}>
            <ChevronRight className="w-4 h-4 text-wood-primary" />
          </Link>
          <motion.div className="absolute inset-0 bg-red-500 opacity-0 group-active:opacity-20 transition-opacity" />
        </motion.div>
      </div>

      <section>
        <h2 className="text-xs font-bold uppercase tracking-[0.2em] text-sandal-base mb-6">Quick Actions</h2>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          {quickActions.map((action, idx) => (
            <Link key={idx} to={action.path}>
              <motion.div
                whileHover={{ y: -5 }}
                whileTap={{ scale: 0.95 }}
                className="glass-card p-5 aspect-square flex flex-col items-center justify-center gap-3 text-center transition-shadow hover:shadow-xl"
              >
                <div className={cn("p-3 rounded-2xl text-white", action.color)}>
                  <action.icon className="w-6 h-6" />
                </div>
                <span className="font-bold text-sm text-sandal-dark">{action.title}</span>
              </motion.div>
            </Link>
          ))}
        </div>
      </section>

      <section>
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-xs font-bold uppercase tracking-[0.2em] text-sandal-base">Recently Added</h2>
          <Link to="/trees" className="text-sm font-bold text-wood-primary flex items-center gap-1">
            View All <ChevronRight className="w-4 h-4" />
          </Link>
        </div>
        
        <div className="space-y-3">
          {loading ? (
            <p className="text-sandal-base italic">Loading trees...</p>
          ) : stats.latestTrees.length > 0 ? (
            stats.latestTrees.map((tree) => (
              <motion.div 
                key={tree.id}
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                className="glass-card p-4 flex items-center gap-4 hover:bg-white transition-colors"
              >
                <div className="w-12 h-12 bg-sandal-light rounded-xl flex items-center justify-center text-wood-secondary">
                  <TreeDeciduous className="w-6 h-6" />
                </div>
                <div className="flex-1">
                  <h3 className="font-bold text-sandal-dark">{tree.name}</h3>
                  <div className="flex gap-3 text-xs text-sandal-base font-medium">
                    <span>{tree.age} yrs</span>
                    <span>•</span>
                    <span>{tree.girth} cm girth</span>
                  </div>
                </div>
                <Link to={`/trees/${tree.id}`} className="p-2 text-sandal-base hover:text-wood-primary px-3 py-1 bg-sandal-light rounded-lg text-xs font-bold uppercase tracking-wider">
                  Details
                </Link>
              </motion.div>
            ))
          ) : (
            <div className="glass-card p-8 text-center bg-transparent border-dashed border-2">
              <p className="text-sandal-base font-medium mb-4">No trees registered yet.</p>
              <Link to="/add-tree" className="text-wood-primary font-bold underline">Register your first tree</Link>
            </div>
          )}
        </div>
      </section>
    </div>
  );
}
