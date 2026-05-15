import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { doc, getDoc, collection, query, where, getDocs, orderBy, addDoc, serverTimestamp } from 'firebase/firestore';
import { db, handleFirestoreError, OperationType } from '../lib/firebase';
import { Tree, GrowthRecord } from '../types';
import { motion } from 'motion/react';
import { ArrowLeft, Edit, Trash2, MapPin, Calendar, Ruler, Info, TrendingUp, Plus, Loader2, Info as InfoIcon, TreeDeciduous } from 'lucide-react';
import { useAuth } from '../App';

export default function TreeDetail() {
  const { id } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [tree, setTree] = useState<Tree | null>(null);
  const [records, setRecords] = useState<GrowthRecord[]>([]);
  const [loading, setLoading] = useState(true);
  const [showAddRecord, setShowAddRecord] = useState(false);
  const [newGirth, setNewGirth] = useState('');

  useEffect(() => {
    async function fetchTreeData() {
      if (!id || !user) return;
      try {
        const treeSnap = await getDoc(doc(db, 'trees', id));
        if (treeSnap.exists()) {
          setTree({ id: treeSnap.id, ...treeSnap.data() } as Tree);
        }

        const q = query(
          collection(db, 'trees', id, 'growthRecords'),
          orderBy('date', 'desc')
        );
        const recordsSnap = await getDocs(q);
        setRecords(recordsSnap.docs.map(doc => ({ id: doc.id, ...doc.data() } as GrowthRecord)));
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    }
    fetchTreeData();
  }, [id, user]);

  const addGrowthRecord = async () => {
    if (!id || !user || !newGirth) return;
    try {
      const recordData = {
        treeId: id,
        girth: Number(newGirth),
        date: new Date().toISOString().split('T')[0],
        userId: user.uid,
        createdAt: serverTimestamp()
      };
      const docRef = await addDoc(collection(db, 'trees', id, 'growthRecords'), recordData);
      setRecords([{ id: docRef.id, ...recordData }, ...records]);
      setShowAddRecord(false);
      setNewGirth('');
    } catch (err) {
      handleFirestoreError(err, OperationType.WRITE, 'growthRecords');
    }
  };

  // Maturity Calculator Logic
  // Sandalwood is generally considered mature for heartwood at 15-20 years or 60cm+ girth
  const calculateMaturity = (age: number, girth: number) => {
    const ageProgress = Math.min((age / 15) * 100, 100);
    const girthProgress = Math.min((girth / 60) * 100, 100);
    const overall = (ageProgress + girthProgress) / 2;
    return Math.round(overall);
  };

  const maturity = tree ? calculateMaturity(tree.age, tree.girth) : 0;

  if (loading) return <div className="p-10 flex justify-center"><Loader2 className="w-10 h-10 animate-spin text-wood-primary" /></div>;
  if (!tree) return <div className="p-10 text-center">Tree not found.</div>;

  return (
    <div className="max-w-4xl mx-auto space-y-8 pb-20">
       <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <button onClick={() => navigate(-1)} className="p-2 bg-white rounded-xl shadow-sm">
            <ArrowLeft className="w-5 h-5 text-sandal-dark" />
          </button>
          <div>
            <h1 className="text-2xl font-bold tracking-tight text-sandal-dark">{tree.name}</h1>
            <p className="text-xs font-bold text-sandal-base uppercase tracking-widest">Registered on {new Date(tree.createdAt).toLocaleDateString()}</p>
          </div>
        </div>
        <div className="flex gap-2">
          <button className="p-2 bg-white rounded-xl shadow-sm text-sandal-base">
            <Edit className="w-5 h-5" />
          </button>
          <button className="p-2 bg-red-50 rounded-xl shadow-sm text-red-500">
            <Trash2 className="w-5 h-5" />
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="md:col-span-2 space-y-6">
          <div className="glass-card overflow-hidden">
            <div className="aspect-video bg-sandal-base/10 relative">
               {tree.photoUrl ? (
                 <img src={tree.photoUrl} alt={tree.name} className="w-full h-full object-cover" />
               ) : (
                 <div className="w-full h-full flex flex-col items-center justify-center text-sandal-base/40">
                   <TreeDeciduous className="w-20 h-20" />
                   <p className="mt-2 font-bold uppercase tracking-wider text-xs">No Photo Available</p>
                 </div>
               )}
               <div className="absolute top-4 left-4 bg-white/90 backdrop-blur px-3 py-1.5 rounded-lg flex items-center gap-2 shadow-sm border border-sandal-base/20">
                 <MapPin className="w-4 h-4 text-red-500" />
                 <span className="text-[10px] font-bold text-sandal-dark">{tree.location.lat.toFixed(4)}, {tree.location.lng.toFixed(4)}</span>
               </div>
            </div>
            
            <div className="p-6 grid grid-cols-2 md:grid-cols-3 gap-6 border-b border-sandal-base/10">
              <div className="space-y-1">
                <div className="flex items-center gap-2 text-sandal-base">
                  <Calendar className="w-4 h-4" />
                  <span className="text-[10px] font-bold uppercase tracking-widest">Age</span>
                </div>
                <p className="text-xl font-bold text-sandal-dark">{tree.age} <span className="text-sm font-medium text-sandal-base tracking-normal">Years</span></p>
              </div>
              <div className="space-y-1">
                <div className="flex items-center gap-2 text-sandal-base">
                  <Ruler className="w-4 h-4" />
                  <span className="text-[10px] font-bold uppercase tracking-widest">Girth</span>
                </div>
                <p className="text-xl font-bold text-sandal-dark">{tree.girth} <span className="text-sm font-medium text-sandal-base tracking-normal">cm</span></p>
              </div>
              <div className="space-y-1 col-span-2 md:col-span-1">
                <div className="flex items-center gap-2 text-sandal-base">
                  <InfoIcon className="w-4 h-4" />
                  <span className="text-[10px] font-bold uppercase tracking-widest">Owner</span>
                </div>
                <p className="text-xl font-bold text-sandal-dark truncate">{tree.farmerName}</p>
              </div>
            </div>

            <div className="p-6">
              <h3 className="text-xs font-bold uppercase tracking-widest text-sandal-base mb-2">Notes & Observations</h3>
              <p className="text-sandal-dark leading-relaxed">
                {tree.notes || "No notes provided for this tree."}
              </p>
            </div>
          </div>

          <section className="space-y-4">
             <div className="flex items-center justify-between">
                <h3 className="text-xs font-bold uppercase tracking-widest text-sandal-base px-1">Growth History</h3>
                <button 
                  onClick={() => setShowAddRecord(!showAddRecord)}
                  className="text-xs font-bold text-wood-primary bg-white px-3 py-1 rounded-lg shadow-sm border border-sandal-base/10 flex items-center gap-1"
                >
                  <Plus className="w-4 h-4" /> Add Record
                </button>
             </div>

             {showAddRecord && (
               <motion.div 
                 initial={{ opacity: 0, y: -10 }}
                 animate={{ opacity: 1, y: 0 }}
                 className="glass-card p-4 flex gap-4 bg-wood-primary/5 border-wood-primary/20"
               >
                 <input 
                   type="number" 
                   placeholder="New Girth (cm)"
                   value={newGirth}
                   onChange={e => setNewGirth(e.target.value)}
                   className="flex-1 bg-white border border-sandal-base/20 rounded-xl px-4 text-sm outline-none focus:ring-2 focus:ring-wood-primary"
                 />
                 <button 
                  onClick={addGrowthRecord}
                  className="bg-wood-primary text-white px-6 py-2 rounded-xl text-sm font-bold shadow-lg shadow-wood-primary/20"
                 >
                   Save
                 </button>
               </motion.div>
             )}

             <div className="space-y-3">
               {records.map((record) => (
                 <div key={record.id} className="glass-card p-4 flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <div className="p-2 bg-sandal-light rounded-lg">
                        <TrendingUp className="w-4 h-4 text-wood-secondary" />
                      </div>
                      <div>
                        <p className="text-sm font-bold text-sandal-dark">{record.girth} cm</p>
                        <p className="text-[10px] text-sandal-base uppercase font-bold">{new Date(record.date).toLocaleDateString()}</p>
                      </div>
                    </div>
                    {records[records.indexOf(record)+1] && (
                       <div className="text-[10px] font-black text-green-600 bg-green-50 px-2 py-0.5 rounded">
                         +{Math.max(0, record.girth - records[records.indexOf(record)+1].girth).toFixed(1)} CM
                       </div>
                    )}
                 </div>
               ))}
               {records.length === 0 && <p className="text-center py-6 text-sandal-base italic text-sm">No growth records yet.</p>}
             </div>
          </section>
        </div>

        <div className="space-y-6">
          <div className="glass-card p-6 bg-wood-primary text-white">
            <h3 className="text-xs font-bold uppercase tracking-[0.2em] opacity-60 mb-1">Maturity Status</h3>
            <div className="flex items-end justify-between mb-4">
               <span className="text-4xl font-black">{maturity}%</span>
               <span className="text-xs font-bold opacity-80 uppercase tracking-widest pb-1">Ready for harvest</span>
            </div>
            
            <div className="h-3 bg-white/20 rounded-full overflow-hidden mb-6">
               <motion.div 
                 initial={{ width: 0 }}
                 animate={{ width: `${maturity}%` }}
                 transition={{ duration: 1 }}
                 className="h-full bg-white shadow-[0_0_20px_rgba(255,255,255,0.4)]"
               />
            </div>

            <div className="space-y-4 pt-4 border-t border-white/10">
               <div className="flex items-start gap-3">
                  <Info className="w-5 h-5 shrink-0 mt-0.5 opacity-60" />
                  <p className="text-[11px] leading-relaxed opacity-80">
                    {maturity < 50 
                      ? "The tree is in early growth stage. Heartwood formation begins around 10-12 years." 
                      : maturity < 80 
                      ? "Tree is showing healthy growth. Core heartwood is developing significantly."
                      : "Approaching peak maturity. Monitor girth carefully for harvest selection."}
                  </p>
               </div>
               <Link 
                to="/growth"
                className="block text-center bg-white text-wood-primary py-3 rounded-xl text-xs font-bold uppercase tracking-widest shadow-lg"
               >
                 View Detailed Tracker
               </Link>
            </div>
          </div>

          <div className="glass-card p-4 space-y-4">
             <h3 className="text-xs font-bold uppercase tracking-widest text-sandal-base">Location Preview</h3>
             <div className="aspect-square bg-sandal-light rounded-xl overflow-hidden relative border border-sandal-base/10">
                <div className="absolute inset-0 flex flex-col items-center justify-center p-6 text-center text-sandal-base opacity-40">
                   <MapPin className="w-10 h-10 mb-2" />
                   <p className="text-[10px] font-bold uppercase tracking-widest">Live Map View Unavailable in Preview</p>
                </div>
                {/* Fallback pattern instead of full map in detail view to save calls */}
             </div>
             <Link 
              to="/map"
              className="text-xs font-bold text-wood-primary flex items-center justify-center gap-2 py-2 bg-sandal-light rounded-lg"
             >
               Open Full Interactive Map
             </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
