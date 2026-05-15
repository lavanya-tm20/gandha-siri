import { useState, useEffect } from 'react';
import { auth, db } from '../lib/firebase';
import { signOut, updateProfile } from 'firebase/auth';
import { doc, getDoc, updateDoc } from 'firebase/firestore';
import { motion } from 'motion/react';
import { User, Mail, Shield, Bell, AppWindow, ChevronRight, LogOut, Save, Camera } from 'lucide-react';
import { useAuth } from '../App';

export default function Settings() {
  const { user } = useAuth();
  const [profile, setProfile] = useState({ name: '', role: 'farmer' });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    async function fetchProfile() {
      if (!user) return;
      const docSnap = await getDoc(doc(db, 'users', user.uid));
      if (docSnap.exists()) {
        setProfile({
          name: docSnap.data().name || user.displayName || '',
          role: docSnap.data().role || 'farmer'
        });
      }
    }
    fetchProfile();
  }, [user]);

  const handleSave = async () => {
    if (!user) return;
    setLoading(true);
    try {
      await updateDoc(doc(db, 'users', user.uid), {
        name: profile.name
      });
      await updateProfile(user, { displayName: profile.name });
      alert('Profile updated successfully!');
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto space-y-8 pb-20">
      <h1 className="text-3xl font-bold text-sandal-dark tracking-tight">Settings & Profile</h1>

      <section className="glass-card p-6">
        <div className="flex items-center gap-6 mb-8">
          <div className="relative">
            <div className="w-24 h-24 bg-wood-primary/10 rounded-3xl flex items-center justify-center text-wood-primary border-2 border-wood-primary/20">
              <User className="w-12 h-12" />
            </div>
            <button className="absolute -bottom-2 -right-2 p-2 bg-white rounded-xl shadow-lg border border-sandal-base/20 text-wood-primary">
              <Camera className="w-4 h-4" />
            </button>
          </div>
          <div>
            <h2 className="text-xl font-bold text-sandal-dark">{profile.name || 'Sandalwood Farmer'}</h2>
            <p className="text-sm text-sandal-base font-medium flex items-center gap-2">
              <Shield className="w-4 h-4 text-green-600" />
              Verified {profile.role}
            </p>
          </div>
        </div>

        <div className="space-y-6">
          <div className="space-y-2">
            <label className="text-xs font-bold uppercase tracking-widest text-sandal-base px-1">Full Name</label>
            <input
              type="text"
              value={profile.name}
              onChange={e => setProfile({ ...profile, name: e.target.value })}
              className="w-full bg-sandal-light border-none rounded-xl py-3 px-4 focus:ring-2 focus:ring-wood-primary outline-none font-medium"
            />
          </div>

          <div className="space-y-2">
            <label className="text-xs font-bold uppercase tracking-widest text-sandal-base px-1">Email Address</label>
            <div className="flex items-center gap-3 bg-sandal-light/50 p-4 rounded-xl text-sandal-base">
              <Mail className="w-5 h-5" />
              <span className="font-medium">{user?.email}</span>
            </div>
          </div>

          <button
            onClick={handleSave}
            disabled={loading}
            className="w-full wood-gradient text-white font-bold py-4 rounded-xl flex items-center justify-center gap-2"
          >
            <Save className="w-5 h-5" />
            {loading ? 'Saving...' : 'Save Profile Changes'}
          </button>
        </div>
      </section>

      <section className="space-y-4">
        <h3 className="text-xs font-bold uppercase tracking-[0.2em] text-sandal-base px-1">App Preferences</h3>
        
        <div className="space-y-2">
          {[
            { icon: Bell, label: 'Notifications', value: 'Enabled' },
            { icon: Shield, label: 'Security Alerts', value: 'Emergency Contacts' },
            { icon: AppWindow, label: 'Offline Sync', value: 'Automatic' }
          ].map((item, idx) => (
            <div key={idx} className="glass-card p-4 flex items-center justify-between cursor-pointer hover:bg-white transition-colors">
              <div className="flex items-center gap-4">
                <div className="p-2 bg-sandal-light rounded-lg text-wood-secondary">
                  <item.icon className="w-5 h-5" />
                </div>
                <span className="font-bold text-sandal-dark">{item.label}</span>
              </div>
              <div className="flex items-center gap-2">
                <span className="text-xs font-bold text-sandal-base uppercase">{item.value}</span>
                <ChevronRight className="w-4 h-4 text-sandal-base" />
              </div>
            </div>
          ))}
        </div>
      </section>

      <button
        onClick={() => signOut(auth)}
        className="w-full p-4 bg-red-50 text-red-600 font-bold rounded-xl flex items-center justify-center gap-2 border border-red-100"
      >
        <LogOut className="w-5 h-5" />
        Sign Out from App
      </button>
    </div>
  );
}
