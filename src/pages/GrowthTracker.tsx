import { useState, useEffect } from 'react';
import { collection, query, where, getDocs, orderBy } from 'firebase/firestore';
import { db, handleFirestoreError, OperationType } from '../lib/firebase';
import { useAuth } from '../App';
import { Tree, GrowthRecord } from '../types';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, AreaChart, Area } from 'recharts';
import { motion } from 'motion/react';
import { TrendingUp, TreeDeciduous, Info, ChevronDown, Calendar } from 'lucide-react';
import { cn } from '../lib/utils';

export default function GrowthTracker() {
  const { user } = useAuth();
  const [trees, setTrees] = useState<Tree[]>([]);
  const [selectedTreeId, setSelectedTreeId] = useState<string>('');
  const [records, setRecords] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function fetchTrees() {
      if (!user) return;
      try {
        const q = query(collection(db, 'trees'), where('userId', '==', user.uid));
        const snap = await getDocs(q);
        const fetchedTrees = snap.docs.map(doc => ({ id: doc.id, ...doc.data() } as Tree));
        setTrees(fetchedTrees);
        if (fetchedTrees.length > 0) setSelectedTreeId(fetchedTrees[0].id);
      } catch (err) {
        handleFirestoreError(err, OperationType.LIST, 'trees');
      } finally {
        setLoading(false);
      }
    }
    fetchTrees();
  }, [user]);

  useEffect(() => {
    async function fetchRecords() {
      if (!selectedTreeId) return;
      try {
        const q = query(
          collection(db, 'trees', selectedTreeId, 'growthRecords'),
          orderBy('date', 'asc')
        );
        const snap = await getDocs(q);
        const fetchedRecords = snap.docs.map(doc => {
          const data = doc.data();
          return {
            date: new Date(data.date).toLocaleDateString('en-US', { month: 'short', year: '2-digit' }),
            girth: data.girth,
            rawDate: data.date
          };
        });
        
        // Add initial record from tree data if no records exist or context needed
        const selectedTree = trees.find(t => t.id === selectedTreeId);
        if (selectedTree && fetchedRecords.length === 0) {
           setRecords([{ 
             date: new Date(selectedTree.createdAt).toLocaleDateString('en-US', { month: 'short', year: '2-digit' }), 
             girth: selectedTree.girth 
           }]);
        } else {
           setRecords(fetchedRecords);
        }
      } catch (err) {
        console.error(err);
      }
    }
    fetchRecords();
  }, [selectedTreeId, trees]);

  const selectedTree = trees.find(t => t.id === selectedTreeId);

  return (
    <div className="space-y-8 max-w-5xl mx-auto pb-20">
      <div>
        <h1 className="text-3xl font-bold tracking-tight text-sandal-dark">Growth Analytics</h1>
        <p className="text-sandal-base font-medium">Visualizing the progress of your sandalwood plantation.</p>
      </div>

      <section className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-1 space-y-6">
           <div className="glass-card p-6">
              <label className="text-xs font-bold uppercase tracking-widest text-sandal-base mb-4 block">Select Tree to Track</label>
              <div className="space-y-2 max-h-[400px] overflow-y-auto pr-2 custom-scrollbar">
                 {trees.map((tree) => (
                   <button
                    key={tree.id}
                    onClick={() => setSelectedTreeId(tree.id)}
                    className={cn(
                      "w-full flex items-center gap-3 p-4 rounded-xl transition-all text-left",
                      selectedTreeId === tree.id 
                        ? "bg-wood-primary text-white shadow-lg shadow-wood-primary/20" 
                        : "bg-sandal-light text-sandal-dark hover:bg-white"
                    )}
                   >
                     <TreeDeciduous className={cn("w-5 h-5", selectedTreeId === tree.id ? "text-white" : "text-wood-secondary")} />
                     <div className="flex-1 min-w-0">
                        <p className="font-bold text-sm truncate">{tree.name}</p>
                        <p className={cn("text-[10px] font-medium opacity-60 uppercase tracking-widest", selectedTreeId === tree.id ? "text-white" : "text-sandal-base")}>
                          {tree.age} Years • {tree.girth} cm
                        </p>
                     </div>
                   </button>
                 ))}
                 {trees.length === 0 && !loading && (
                   <p className="text-sm text-sandal-base italic text-center py-4">No trees found.</p>
                 )}
              </div>
           </div>

           {selectedTree && (
             <motion.div 
               initial={{ opacity: 0, x: -20 }}
               animate={{ opacity: 1, x: 0 }}
               className="glass-card p-6 bg-wood-primary/5 border-wood-primary/20"
             >
                <div className="flex items-center gap-3 mb-6">
                   <div className="p-3 bg-wood-primary/10 rounded-2xl text-wood-primary">
                      <TrendingUp className="w-6 h-6" />
                   </div>
                   <div>
                      <h4 className="font-bold text-sandal-dark">Growth Summary</h4>
                      <p className="text-xs text-sandal-base">{selectedTree.name}</p>
                   </div>
                </div>

                <div className="space-y-4">
                   <div className="flex justify-between items-center bg-white p-3 rounded-xl">
                      <span className="text-[10px] font-bold text-sandal-base uppercase tracking-widest">Initial Girth</span>
                      <span className="font-black text-sandal-dark">{selectedTree.girth} cm</span>
                   </div>
                   <div className="flex justify-between items-center bg-white p-3 rounded-xl">
                      <span className="text-[10px] font-bold text-sandal-base uppercase tracking-widest">Current Girth</span>
                      <span className="font-black text-wood-primary">
                         {records.length > 0 ? records[records.length - 1].girth : selectedTree.girth} cm
                      </span>
                   </div>
                   <div className="flex justify-between items-center bg-green-50 p-3 rounded-xl border border-green-200">
                      <span className="text-[10px] font-bold text-green-700 uppercase tracking-widest">Net Increase</span>
                      <span className="font-black text-green-700">
                         {records.length > 0 ? (records[records.length - 1].girth - selectedTree.girth).toFixed(1) : '0.0'} cm
                      </span>
                   </div>
                </div>
             </motion.div>
           )}
        </div>

        <div className="lg:col-span-2 space-y-6">
           <div className="glass-card p-8 h-[450px]">
              <div className="flex items-center justify-between mb-8">
                 <div>
                    <h3 className="font-bold text-lg text-sandal-dark">Girth Increase History</h3>
                    <p className="text-xs text-sandal-base font-medium">Tracking diameter changes over time (cm)</p>
                 </div>
                 <div className="flex items-center gap-2 bg-sandal-light px-3 py-1.5 rounded-lg">
                    <Calendar className="w-4 h-4 text-wood-secondary" />
                    <span className="text-[10px] font-bold text-sandal-dark uppercase tracking-widest">Timeline</span>
                 </div>
              </div>

              {records.length > 0 ? (
                <div className="w-full h-72">
                  <ResponsiveContainer width="100%" height="100%">
                    <AreaChart data={records}>
                      <defs>
                        <linearGradient id="colorGirth" x1="0" y1="0" x2="0" y2="1">
                          <stop offset="5%" stopColor="#5D4037" stopOpacity={0.2}/>
                          <stop offset="95%" stopColor="#5D4037" stopOpacity={0}/>
                        </linearGradient>
                      </defs>
                      <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#BCAAA4" opacity={0.2} />
                      <XAxis 
                        dataKey="date" 
                        axisLine={false} 
                        tickLine={false} 
                        tick={{fontSize: 10, fontWeight: 700, fill: '#8D6E63'}} 
                        dy={10}
                      />
                      <YAxis 
                        axisLine={false} 
                        tickLine={false} 
                        tick={{fontSize: 10, fontWeight: 700, fill: '#8D6E63'}} 
                        tickFormatter={(val) => `${val}cm`}
                      />
                      <Tooltip 
                        contentStyle={{ 
                          borderRadius: '12px', 
                          border: 'none', 
                          boxShadow: '0 10px 20px rgba(0,0,0,0.1)',
                          backgroundColor: '#5D4037',
                          color: '#fff'
                        }}
                        itemStyle={{ color: '#fff', fontSize: '12px', fontWeight: '800' }}
                        labelStyle={{ display: 'none' }}
                      />
                      <Area 
                        type="monotone" 
                        dataKey="girth" 
                        stroke="#5D4037" 
                        strokeWidth={3} 
                        fillOpacity={1} 
                        fill="url(#colorGirth)" 
                      />
                    </AreaChart>
                  </ResponsiveContainer>
                </div>
              ) : (
                <div className="h-64 flex flex-col items-center justify-center text-sandal-base gap-3 border-2 border-dashed border-sandal-base/20 rounded-2xl">
                   <TrendingUp className="w-12 h-12 opacity-20" />
                   <p className="font-bold text-sm uppercase tracking-widest">Select a tree to view data</p>
                </div>
              )}
           </div>

           <div className="glass-card p-6 flex items-start gap-4 bg-orange-50 border-orange-200">
              <div className="p-3 bg-orange-100 rounded-2xl text-orange-600">
                 <Info className="w-6 h-6" />
              </div>
              <div className="space-y-1">
                 <h4 className="font-bold text-orange-800">Observation Tip</h4>
                 <p className="text-sm text-orange-700 leading-relaxed">
                   Sandalwood growth typically accelerates after the 8th year mark once the host plants are well established. 
                   Ensure regular watering during dry seasons to maintain steady girth increase.
                 </p>
              </div>
           </div>
        </div>
      </section>
    </div>
  );
}
