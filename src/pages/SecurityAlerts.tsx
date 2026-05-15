import React, { useState, useEffect } from 'react';
import { collection, query, where, getDocs, orderBy, updateDoc, doc } from 'firebase/firestore';
import { db } from '../lib/firebase';
import { useAuth } from '../App';
import { motion, AnimatePresence } from 'motion/react';
import { ShieldAlert, Clock, CheckCircle2, ChevronRight, AlertTriangle, Filter } from 'lucide-react';
import { cn } from '../lib/utils';

interface SecurityAlert {
  id: string;
  type: string;
  status: 'pending' | 'resolved' | 'investigating';
  createdAt: string;
  userId: string;
  location?: { lat: number, lng: number } | null;
}

export default function SecurityAlerts() {
  const { user } = useAuth();
  const [alerts, setAlerts] = useState<SecurityAlert[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState<'all' | 'pending' | 'resolved'>('all');

  useEffect(() => {
    async function fetchAlerts() {
      if (!user) return;
      try {
        const q = query(
          collection(db, 'alerts'),
          where('userId', '==', user.uid),
          orderBy('createdAt', 'desc')
        );
        const snap = await getDocs(q);
        setAlerts(snap.docs.map(doc => ({ id: doc.id, ...doc.data() } as SecurityAlert)));
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    }
    fetchAlerts();
  }, [user]);

  const resolveAlert = async (alertId: string) => {
    try {
      await updateDoc(doc(db, 'alerts', alertId), { status: 'resolved' });
      setAlerts(alerts.map(a => a.id === alertId ? { ...a, status: 'resolved' as const } : a));
    } catch (err) {
      console.error(err);
    }
  };

  const filteredAlerts = alerts.filter(a => filter === 'all' ? true : a.status === filter);

  return (
    <div className="max-w-3xl mx-auto space-y-8 pb-20">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-sandal-dark tracking-tight">Security Center</h1>
          <p className="text-sandal-base font-medium">History and status of triggered alarms.</p>
        </div>
        
        <div className="flex items-center gap-2 bg-white p-1 rounded-xl shadow-sm border border-sandal-base/10">
          {(['all', 'pending', 'resolved'] as const).map((t) => (
            <button
              key={t}
              onClick={() => setFilter(t)}
              className={cn(
                "px-4 py-2 rounded-lg text-xs font-bold uppercase tracking-wider transition-all",
                filter === t ? "bg-wood-primary text-white shadow-md" : "text-sandal-base hover:bg-sandal-light"
              )}
            >
              {t}
            </button>
          ))}
        </div>
      </div>

      <div className="space-y-4">
        {loading ? (
          <div className="py-20 text-center animate-pulse text-sandal-base font-bold uppercase tracking-[0.2em]">
            Syncing Security Logs...
          </div>
        ) : filteredAlerts.length > 0 ? (
          <AnimatePresence mode="popLayout">
            {filteredAlerts.map((alert) => (
              <motion.div
                key={alert.id}
                layout
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, scale: 0.95 }}
                className={cn(
                  "glass-card p-6 border-l-4",
                  alert.status === 'pending' ? "border-l-red-500" : "border-l-green-500"
                )}
              >
                <div className="flex items-start justify-between gap-4">
                  <div className="flex gap-4">
                    <div className={cn(
                      "w-12 h-12 rounded-2xl flex items-center justify-center shrink-0",
                      alert.status === 'pending' ? "bg-red-50 text-red-500" : "bg-green-50 text-green-500"
                    )}>
                      {alert.status === 'pending' ? <ShieldAlert className="w-6 h-6" /> : <CheckCircle2 className="w-6 h-6" />}
                    </div>
                    <div>
                      <div className="flex items-center gap-2 mb-1">
                        <h3 className="font-bold text-sandal-dark">Panic Alarm Triggered</h3>
                        <span className={cn(
                          "text-[10px] font-black uppercase tracking-widest px-2 py-0.5 rounded",
                          alert.status === 'pending' ? "bg-red-100 text-red-700" : "bg-green-100 text-green-700"
                        )}>
                          {alert.status}
                        </span>
                      </div>
                      <div className="flex items-center gap-4 text-xs font-medium text-sandal-base">
                        <span className="flex items-center gap-1.5"><Clock className="w-3.5 h-3.5" /> {new Date(alert.createdAt).toLocaleString()}</span>
                        <span className="flex items-center gap-1.5"><AlertTriangle className="w-3.5 h-3.5" /> Potential Theft</span>
                      </div>
                    </div>
                  </div>
                  
                  {alert.status === 'pending' && (
                    <button
                      onClick={() => resolveAlert(alert.id)}
                      className="px-4 py-2 bg-sandal-light hover:bg-white text-sandal-dark font-bold text-xs rounded-xl shadow-sm border border-sandal-base/10 transition-all uppercase tracking-wider"
                    >
                      Resolve Status
                    </button>
                  )}
                </div>
              </motion.div>
            ))}
          </AnimatePresence>
        ) : (
          <div className="glass-card p-12 text-center border-dashed border-2 bg-transparent">
            <ShieldAlert className="w-12 h-12 text-sandal-base/20 mx-auto mb-4" />
            <p className="text-sandal-dark font-bold">No security alerts found.</p>
            <p className="text-sm text-sandal-base">Your plantation security status is currently healthy.</p>
          </div>
        )}
      </div>

      <div className="glass-card p-6 bg-red-50/50 border-red-100">
        <h4 className="flex items-center gap-2 text-red-600 font-bold text-sm mb-2">
          <AlertTriangle className="w-4 h-4" /> EMERGENCY PROTOCOL
        </h4>
        <p className="text-xs text-sandal-base leading-relaxed">
          When a panic alert is triggered, the system automatically simulates notifications to the local forest guard and your verified emergency contacts. In a real-world scenario, this would trigger IoT sensors or direct satellite communication.
        </p>
      </div>
    </div>
  );
}
