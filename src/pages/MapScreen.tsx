import { useState, useEffect } from 'react';
import { APIProvider, Map, AdvancedMarker, Pin, Marker } from '@vis.gl/react-google-maps';
import { collection, query, where, getDocs } from 'firebase/firestore';
import { db } from '../lib/firebase';
import { useAuth } from '../App';
import { Tree } from '../types';
import { motion } from 'motion/react';
import { TreeDeciduous, MapPin, X, ChevronRight, Navigation } from 'lucide-react';
import { Link } from 'react-router-dom';
import { Geolocation } from '@capacitor/geolocation';

const API_KEY = import.meta.env.VITE_GOOGLE_MAPS_API_KEY || process.env.GOOGLE_MAPS_PLATFORM_KEY || '';
const MAP_ID = import.meta.env.VITE_GOOGLE_MAPS_MAP_ID;
// Only use Map ID if it's explicitly set to something that doesn't look like a placeholder
// AND if the user hasn't had issues (fallback to regular markers if so)
const isValidMapId = Boolean(
  MAP_ID && 
  MAP_ID !== 'null' && 
  MAP_ID !== 'undefined' &&
  MAP_ID !== '' && 
  MAP_ID.length > 5 && 
  !MAP_ID.includes('YOUR_MAP_ID')
);
const hasValidKey = Boolean(API_KEY) && API_KEY !== 'YOUR_API_KEY' && API_KEY.length > 10;

export default function MapScreen() {
  const { user } = useAuth();
  const [trees, setTrees] = useState<Tree[]>([]);
  const [selectedTree, setSelectedTree] = useState<Tree | null>(null);
  const [loading, setLoading] = useState(true);
  const [userLocation, setUserLocation] = useState<{lat: number, lng: number} | null>(null);

  useEffect(() => {
    async function fetchTrees() {
      if (!user) return;
      try {
        const q = query(collection(db, 'trees'), where('userId', '==', user.uid));
        const snap = await getDocs(q);
        setTrees(snap.docs.map(doc => ({ id: doc.id, ...doc.data() } as Tree)));
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    }

    async function getUserLocation() {
      try {
        const position = await Geolocation.getCurrentPosition();
        setUserLocation({
          lat: position.coords.latitude,
          lng: position.coords.longitude
        });
      } catch (err) {
        console.warn('Could not get geolocation:', err);
      }
    }

    fetchTrees();
    getUserLocation();
  }, [user]);

  if (!hasValidKey) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[70vh] p-6 text-center">
        <div className="w-20 h-20 bg-wood-primary/10 rounded-3xl flex items-center justify-center text-wood-primary mb-6">
          <MapPin className="w-10 h-10" />
        </div>
        <h2 className="text-2xl font-bold text-sandal-dark mb-4">Map Configuration</h2>
        <p className="text-sandal-base max-w-md mx-auto mb-8 text-sm">
          To enable the interactive map, please ensure a valid Google Maps API Key is configured in your platform settings.
        </p>
        <div className="p-4 bg-sandal-light/30 rounded-2xl border border-sandal-base/10 text-xs text-sandal-dark/60 italic leading-relaxed">
           Check your Cloud Console to ensure "Maps JavaScript API" is enabled for your API key.
        </div>
      </div>
    );
  }

  const defaultCenter = userLocation || (trees.length > 0 ? trees[0].location : { lat: 12.9716, lng: 77.5946 });

  return (
    <div className="h-[calc(100vh-160px)] md:h-[calc(100vh-80px)] -m-6 relative rounded-3xl overflow-hidden shadow-2xl border-4 border-white">
      <APIProvider apiKey={API_KEY} version="weekly">
        <Map
          defaultCenter={defaultCenter}
          defaultZoom={12}
          mapId={isValidMapId ? MAP_ID : undefined}
          internalUsageAttributionIds={['gmp_mcp_codeassist_v1_aistudio']}
          gestureHandling={'greedy'}
          disableDefaultUI={true}
          style={{ width: '100%', height: '100%' }}
        >
          {userLocation && (
            <Marker 
              position={userLocation} 
              icon={{
                path: 0, // CIRCLE
                scale: 10,
                fillColor: '#4285F4',
                fillOpacity: 1,
                strokeWeight: 4,
                strokeColor: 'white'
              }}
              title="You are here"
            />
          )}

          {trees.map((tree) => (
            isValidMapId ? (
              <AdvancedMarker 
                key={tree.id} 
                position={tree.location}
                onClick={() => setSelectedTree(tree)}
                title={tree.name}
              >
                <Pin background="#2193b0" glyphColor="#fff" />
              </AdvancedMarker>
            ) : (
              <Marker 
                key={tree.id} 
                position={tree.location}
                onClick={() => setSelectedTree(tree)}
                title={tree.name}
              />
            )
          ))}
        </Map>
      </APIProvider>

      <div className="absolute top-6 left-6 z-10 pointer-events-none">
         <div className="bg-white/90 backdrop-blur rounded-2xl p-4 shadow-xl border border-sandal-base/20 max-w-[240px] pointer-events-auto">
            <h2 className="text-xs font-black uppercase tracking-[0.2em] text-wood-primary mb-1">Forest Coverage</h2>
            <p className="text-3xl font-black text-sandal-dark">{trees.length}</p>
            <p className="text-[10px] text-sandal-base font-bold uppercase tracking-widest">Trees tagged in region</p>
         </div>
      </div>
      
      {userLocation && (
        <button 
          onClick={() => {
            // Recenter logic would go here, for now just a UI cue
          }}
          className="absolute top-6 right-6 z-10 bg-white p-3 rounded-2xl shadow-xl border border-sandal-base/20 text-wood-primary"
        >
          <Navigation className="w-5 h-5" />
        </button>
      )}

      {selectedTree && (
        <motion.div 
          initial={{ y: 100, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          className="absolute bottom-10 left-6 right-6 md:left-auto md:right-10 md:w-96 z-20"
        >
           <div className="glass-card shadow-2xl bg-white p-5 overflow-hidden">
              <button 
                onClick={() => setSelectedTree(null)}
                className="absolute top-4 right-4 p-1 rounded-lg bg-sandal-light text-sandal-base"
              >
                <X className="w-4 h-4" />
              </button>
              
              <div className="flex gap-4">
                 <div className="w-20 h-20 bg-sandal-light rounded-xl flex items-center justify-center text-wood-secondary overflow-hidden shrink-0">
                    {selectedTree.photoUrl ? (
                      <img src={selectedTree.photoUrl} alt={selectedTree.name} className="w-full h-full object-cover" />
                    ) : (
                      <TreeDeciduous className="w-10 h-10 opacity-40" />
                    )}
                 </div>
                 <div className="flex-1 min-w-0 pr-6">
                    <h3 className="font-bold text-sandal-dark text-lg truncate mb-1">{selectedTree.name}</h3>
                    <div className="flex gap-4 text-[10px] font-black tracking-widest text-sandal-base uppercase mb-3">
                       <span>{selectedTree.age} YRS</span>
                       <span>{selectedTree.girth} CM</span>
                    </div>
                    <Link 
                      to={`/trees/${selectedTree.id}`}
                      className="inline-flex items-center gap-2 bg-wood-primary text-white px-4 py-2 rounded-lg text-xs font-bold uppercase tracking-wider"
                    >
                      View Profile <ChevronRight className="w-4 h-4" />
                    </Link>
                 </div>
              </div>
           </div>
        </motion.div>
      )}
    </div>
  );
}
