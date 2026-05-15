import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { collection, addDoc, serverTimestamp } from 'firebase/firestore';
import { db, handleFirestoreError, OperationType } from '../lib/firebase';
import { useAuth } from '../App';
import { motion } from 'motion/react';
import { Camera as CameraIcon, MapPin, Save, ArrowLeft, Loader2, TreeDeciduous } from 'lucide-react';
import { cn } from '../lib/utils';

import { Camera as NativeCamera, CameraResultType, CameraSource } from '@capacitor/camera';
import { Geolocation } from '@capacitor/geolocation';

export default function AddTree() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [gettingLocation, setGettingLocation] = useState(false);
  
  const [formData, setFormData] = useState({
    name: '',
    age: '',
    girth: '',
    farmerName: user?.displayName || '',
    notes: '',
    location: null as { lat: number, lng: number } | null,
    photoUrl: ''
  });

  const captureLocation = async () => {
    setGettingLocation(true);
    try {
      const position = await Geolocation.getCurrentPosition({
        enableHighAccuracy: true,
        timeout: 10000
      });
      setFormData(prev => ({ 
        ...prev, 
        location: { lat: position.coords.latitude, lng: position.coords.longitude } 
      }));
    } catch (err) {
      console.error(err);
      alert('Could not capture location. Please ensure GPS is enabled and permissions granted.');
    } finally {
      setGettingLocation(false);
    }
  };

  const capturePhoto = async () => {
    try {
      const image = await NativeCamera.getPhoto({
        quality: 90,
        allowEditing: false,
        resultType: CameraResultType.DataUrl,
        source: CameraSource.Camera
      });
      
      if (image.dataUrl) {
        setFormData(prev => ({ ...prev, photoUrl: image.dataUrl! }));
      }
    } catch (err) {
      console.info('User cancelled photo capture or error:', (err as Error).message);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.location) {
      alert('Please capture current location first.');
      return;
    }

    if (!user) {
      alert('You must be signed in to register a tree.');
      return;
    }

    setLoading(true);
    try {
      const treePath = 'trees';
      const treeData = {
        name: formData.name || 'Unnamed Tree',
        age: Number(formData.age) || 0,
        girth: Number(formData.girth) || 0,
        farmerName: formData.farmerName || user.displayName || 'Farmer',
        notes: formData.notes || '',
        photoUrl: formData.photoUrl || '',
        location: formData.location,
        userId: user.uid,
        createdAt: serverTimestamp()
      };
      
      await addDoc(collection(db, treePath), treeData);
      navigate('/trees');
    } catch (err) {
      handleFirestoreError(err, OperationType.WRITE, 'trees');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto pb-20">
      <div className="flex items-center gap-4 mb-8">
        <button onClick={() => navigate(-1)} className="p-2 bg-white rounded-xl shadow-sm text-sandal-dark">
          <ArrowLeft className="w-5 h-5" />
        </button>
        <h1 className="text-2xl font-bold tracking-tight text-sandal-dark">Register New Tree</h1>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        <motion.div 
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          className="glass-card p-6 space-y-6"
        >
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-2">
              <label className="text-xs font-bold uppercase tracking-widest text-sandal-base px-1">Tree Name / Tag ID</label>
              <input
                required
                type="text"
                placeholder="e.g. SN-2024-001"
                className="w-full bg-sandal-light/50 border-none rounded-xl py-3 px-4 focus:ring-2 focus:ring-wood-primary outline-none text-sandal-dark"
                value={formData.name || ''}
                onChange={e => setFormData({ ...formData, name: e.target.value })}
              />
            </div>
            <div className="space-y-2">
              <label className="text-xs font-bold uppercase tracking-widest text-sandal-base px-1">Farmer Name</label>
              <input
                required
                type="text"
                className="w-full bg-sandal-light/50 border-none rounded-xl py-3 px-4 focus:ring-2 focus:ring-wood-primary outline-none text-sandal-dark"
                value={formData.farmerName || ''}
                onChange={e => setFormData({ ...formData, farmerName: e.target.value })}
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-6">
            <div className="space-y-2">
              <label className="text-xs font-bold uppercase tracking-widest text-sandal-base px-1">Age (Years)</label>
              <input
                required
                type="number"
                placeholder="0"
                className="w-full bg-sandal-light/50 border-none rounded-xl py-3 px-4 focus:ring-2 focus:ring-wood-primary outline-none text-sandal-dark"
                value={formData.age || ''}
                onChange={e => setFormData({ ...formData, age: e.target.value })}
              />
            </div>
            <div className="space-y-2">
              <label className="text-xs font-bold uppercase tracking-widest text-sandal-base px-1">Girth (CM)</label>
              <input
                required
                type="number"
                placeholder="0.0"
                step="0.1"
                className="w-full bg-sandal-light/50 border-none rounded-xl py-3 px-4 focus:ring-2 focus:ring-wood-primary outline-none text-sandal-dark"
                value={formData.girth || ''}
                onChange={e => setFormData({ ...formData, girth: e.target.value })}
              />
            </div>
          </div>

          <div className="space-y-4">
             <label className="text-xs font-bold uppercase tracking-widest text-sandal-base px-1">Tree Location</label>
             <div className="flex items-center gap-4">
                <button
                  type="button"
                  onClick={captureLocation}
                  disabled={gettingLocation}
                  className={cn(
                    "flex-1 flex items-center justify-center gap-2 py-4 rounded-xl font-bold transition-all",
                    formData.location ? "bg-green-100 text-green-700 border-2 border-green-200" : "bg-sandal-light text-wood-primary border-2 border-dashed border-wood-primary/30"
                  )}
                >
                  {gettingLocation ? <Loader2 className="w-5 h-5 animate-spin" /> : <MapPin className="w-5 h-5" />}
                  {formData.location ? "Location Captured" : "Capture Current Location"}
                </button>
                {formData.location && (
                  <div className="text-[10px] font-mono text-sandal-base bg-sandal-light p-2 rounded-lg leading-tight">
                    LAT: {formData.location.lat.toFixed(6)}<br/>
                    LNG: {formData.location.lng.toFixed(6)}
                  </div>
                )}
             </div>
          </div>

          <div className="space-y-4">
             <label className="text-xs font-bold uppercase tracking-widest text-sandal-base px-1">Tree Photo</label>
             <div 
               onClick={capturePhoto}
               className="w-full aspect-video bg-sandal-light rounded-2xl border-2 border-dashed border-sandal-base/20 flex flex-col items-center justify-center gap-3 text-sandal-base cursor-pointer hover:bg-sandal-light/80 transition-colors relative overflow-hidden"
             >
                {formData.photoUrl ? (
                  <img src={formData.photoUrl} className="absolute inset-0 w-full h-full object-cover" alt="Preview" />
                ) : (
                  <>
                    <CameraIcon className="w-10 h-10" />
                    <span className="text-sm font-bold uppercase tracking-wider">Tap to capture tree photo</span>
                    <span className="text-[10px]">(Native Camera)</span>
                  </>
                )}
             </div>
          </div>

          <div className="space-y-2">
            <label className="text-xs font-bold uppercase tracking-widest text-sandal-base px-1">Notes / Care History</label>
            <textarea
              className="w-full bg-sandal-light/50 border-none rounded-xl py-3 px-4 focus:ring-2 focus:ring-wood-primary outline-none min-h-[100px] text-sandal-dark"
              placeholder="Any specific care instructions..."
              value={formData.notes || ''}
              onChange={e => setFormData({ ...formData, notes: e.target.value })}
            ></textarea>
          </div>
        </motion.div>

        <button
          type="submit"
          disabled={loading}
          className="w-full wood-gradient text-white font-bold py-5 rounded-2xl shadow-xl shadow-wood-primary/30 flex items-center justify-center gap-2 disabled:opacity-50 mb-10"
        >
          {loading ? (
            <Loader2 className="w-6 h-6 animate-spin" />
          ) : (
            <>
              <Save className="w-6 h-6" />
              Save Tree Registration
            </>
          )}
        </button>
      </form>
    </div>
  );
}
