import { useState, useEffect } from 'react';
import { collection, query, where, getDocs, orderBy } from 'firebase/firestore';
import { db, handleFirestoreError, OperationType } from '../lib/firebase';
import { useAuth } from '../App';
import { Tree } from '../types';
import { motion, AnimatePresence } from 'motion/react';
import { Search, Filter, TreeDeciduous, ChevronRight, SlidersHorizontal, ArrowUpDown } from 'lucide-react';
import { Link } from 'react-router-dom';

export default function TreeList() {
  const { user } = useAuth();
  const [trees, setTrees] = useState<Tree[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [sortBy, setSortBy] = useState<'newest' | 'oldest' | 'girth'>('newest');

  useEffect(() => {
    async function fetchTrees() {
      if (!user) return;
      try {
        const q = query(
          collection(db, 'trees'),
          where('userId', '==', user.uid)
        );
        const snap = await getDocs(q);
        const fetchedTrees = snap.docs.map(doc => ({ id: doc.id, ...doc.data() } as Tree));
        setTrees(fetchedTrees);
      } catch (err) {
        handleFirestoreError(err, OperationType.LIST, 'trees');
      } finally {
        setLoading(false);
      }
    }
    fetchTrees();
  }, [user]);

  const filteredTrees = trees
    .filter(tree => tree.name.toLowerCase().includes(searchTerm.toLowerCase()))
    .sort((a, b) => {
      if (sortBy === 'newest') return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
      if (sortBy === 'oldest') return new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime();
      if (sortBy === 'girth') return b.girth - a.girth;
      return 0;
    });

  return (
    <div className="space-y-6 max-w-4xl mx-auto">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold tracking-tight text-sandal-dark">My Registered Trees</h1>
        <span className="bg-wood-primary/10 text-wood-primary text-xs font-bold px-3 py-1 rounded-full">
          {filteredTrees.length} Total
        </span>
      </div>

      <div className="flex flex-col md:flex-row gap-4">
        <div className="relative flex-1">
          <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-sandal-base" />
          <input
            type="text"
            placeholder="Search by name or Tag ID..."
            className="w-full bg-white border border-sandal-base/10 rounded-xl py-3 pl-12 pr-4 focus:ring-2 focus:ring-wood-primary outline-none transition-all"
            value={searchTerm}
            onChange={e => setSearchTerm(e.target.value)}
          />
        </div>
        <div className="flex gap-2">
          <select 
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value as any)}
            className="bg-white border border-sandal-base/10 rounded-xl py-3 px-4 font-bold text-sm text-sandal-dark outline-none cursor-pointer"
          >
            <option value="newest">Newest First</option>
            <option value="oldest">Oldest First</option>
            <option value="girth">Largest Girth</option>
          </select>
          <button className="p-3 bg-white border border-sandal-base/10 rounded-xl text-sandal-base">
            <SlidersHorizontal className="w-5 h-5" />
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <AnimatePresence mode="popLayout">
          {loading ? (
            Array(4).fill(0).map((_, i) => (
              <div key={i} className="glass-card p-4 animate-pulse h-28 bg-white/50" />
            ))
          ) : filteredTrees.length > 0 ? (
            filteredTrees.map((tree, idx) => (
              <motion.div
                key={tree.id}
                layout
                initial={{ opacity: 0, scale: 0.9 }}
                animate={{ opacity: 1, scale: 1 }}
                exit={{ opacity: 0, scale: 0.9 }}
                transition={{ duration: 0.2, delay: idx * 0.05 }}
              >
                <Link to={`/trees/${tree.id}`}>
                  <div className="glass-card p-4 flex gap-4 hover:shadow-lg hover:border-wood-primary/30 transition-all group">
                    <div className="w-20 h-20 bg-sandal-light rounded-2xl flex items-center justify-center text-wood-secondary overflow-hidden">
                      {tree.photoUrl ? (
                         <img src={tree.photoUrl} alt={tree.name} className="w-full h-full object-cover" />
                      ) : (
                         <TreeDeciduous className="w-10 h-10 opacity-40" />
                      )}
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-start justify-between">
                        <h3 className="font-bold text-sandal-dark text-lg truncate group-hover:text-wood-primary transition-colors">{tree.name}</h3>
                        <ChevronRight className="w-5 h-5 text-sandal-base opacity-0 group-hover:opacity-100 transition-all -translate-x-2 group-hover:translate-x-0" />
                      </div>
                      <p className="text-xs font-bold text-sandal-base uppercase tracking-widest">{tree.farmerName}</p>
                      <div className="mt-2 flex gap-3">
                        <div className="text-[10px] font-black bg-green-100 text-green-700 px-2 py-0.5 rounded">
                          {tree.age} YRS
                        </div>
                        <div className="text-[10px] font-black bg-orange-100 text-orange-700 px-2 py-0.5 rounded">
                          {tree.girth}CM
                        </div>
                      </div>
                    </div>
                  </div>
                </Link>
              </motion.div>
            ))
          ) : (
            <div className="col-span-full py-20 text-center">
              <TreeDeciduous className="w-16 h-16 text-sandal-base mx-auto mb-4 opacity-20" />
              <h3 className="text-xl font-bold text-sandal-dark">No trees match your search</h3>
              <p className="text-sandal-base">Try adjusting your filters or search term.</p>
            </div>
          )}
        </AnimatePresence>
      </div>
    </div>
  );
}
