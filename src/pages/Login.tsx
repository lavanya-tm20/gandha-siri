import React, { useState } from 'react';
import { signInWithEmailAndPassword, createUserWithEmailAndPassword, signInWithPopup, GoogleAuthProvider } from 'firebase/auth';
import { auth, db } from '../lib/firebase';
import { doc, setDoc, getDoc, serverTimestamp } from 'firebase/firestore';
import { motion } from 'motion/react';
import { TreeDeciduous, Mail, Lock, LogIn, Github as Google } from 'lucide-react';
import { cn } from '../lib/utils';

export default function Login() {
  const [isLogin, setIsLogin] = useState(true);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleAuth = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      if (isLogin) {
        await signInWithEmailAndPassword(auth, email, password);
      } else {
        const userCred = await createUserWithEmailAndPassword(auth, email, password);
        await setDoc(doc(db, 'users', userCred.user.uid), {
          userId: userCred.user.uid,
          email: userCred.user.email,
          role: 'farmer',
          createdAt: serverTimestamp()
        });
      }
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleSignIn = async () => {
    const provider = new GoogleAuthProvider();
    try {
      const result = await signInWithPopup(auth, provider);
      const userRef = doc(db, 'users', result.user.uid);
      const userSnap = await getDoc(userRef);
      
      if (!userSnap.exists()) {
        await setDoc(userRef, {
          userId: result.user.uid,
          email: result.user.email,
          role: 'farmer',
          createdAt: serverTimestamp()
        });
      }
    } catch (err: any) {
      setError(err.message);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-sandal-light p-6">
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="w-full max-w-md"
      >
        <div className="text-center mb-10">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-wood-primary text-white rounded-3xl shadow-xl shadow-wood-primary/20 mb-4">
            <TreeDeciduous className="w-8 h-8" />
          </div>
          <h1 className="text-3xl font-bold text-sandal-dark tracking-tight">Gandha-Siri</h1>
          <p className="text-sandal-base font-medium mt-1">Sandalwood Farmer's Guard</p>
        </div>

        <div className="bg-white rounded-3xl p-8 shadow-2xl shadow-black/5 border border-sandal-base/10">
          <div className="flex bg-sandal-light p-1 rounded-xl mb-8">
            <button
              onClick={() => setIsLogin(true)}
              className={cn(
                "flex-1 py-2 text-sm font-semibold rounded-lg transition-all",
                isLogin ? "bg-white text-wood-primary shadow-sm" : "text-sandal-base"
              )}
            >
              Login
            </button>
            <button
              onClick={() => setIsLogin(false)}
              className={cn(
                "flex-1 py-2 text-sm font-semibold rounded-lg transition-all",
                !isLogin ? "bg-white text-wood-primary shadow-sm" : "text-sandal-base"
              )}
            >
              Register
            </button>
          </div>

          <form onSubmit={handleAuth} className="space-y-4">
            <div>
              <label className="block text-xs font-bold uppercase tracking-widest text-sandal-base mb-2 px-1">Email Address</label>
              <div className="relative">
                <Mail className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-sandal-base" />
                <input
                  type="email"
                  value={email || ''}
                  onChange={(e) => setEmail(e.target.value)}
                  className="w-full bg-sandal-light border-none rounded-xl py-4 pl-12 pr-4 focus:ring-2 focus:ring-wood-primary transition-all outline-none"
                  placeholder="name@example.com"
                  required
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-bold uppercase tracking-widest text-sandal-base mb-2 px-1">Password</label>
              <div className="relative">
                <Lock className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-sandal-base" />
                <input
                  type="password"
                  value={password || ''}
                  onChange={(e) => setPassword(e.target.value)}
                  className="w-full bg-sandal-light border-none rounded-xl py-4 pl-12 pr-4 focus:ring-2 focus:ring-wood-primary transition-all outline-none"
                  placeholder="••••••••"
                  required
                />
              </div>
            </div>

            {error && (
              <p className="text-red-500 text-sm font-medium px-1 bg-red-50 py-2 rounded-lg text-center">{error}</p>
            )}

            <button
              type="submit"
              disabled={loading}
              className="w-full wood-gradient text-white font-bold py-4 rounded-xl shadow-lg shadow-wood-primary/30 hover:opacity-90 transition-all disabled:opacity-50 flex items-center justify-center gap-2"
            >
              {loading ? "Processing..." : (
                <>
                  <LogIn className="w-5 h-5" />
                  {isLogin ? "Sign In" : "Create Account"}
                </>
              )}
            </button>
          </form>

          <div className="relative my-8">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-sandal-base/20"></div>
            </div>
            <div className="relative flex justify-center text-xs uppercase tracking-widest bg-white px-4 text-sandal-base">
              Or continue with
            </div>
          </div>

          <button
            onClick={handleGoogleSignIn}
            className="w-full bg-white border border-sandal-base/20 text-sandal-dark font-bold py-4 rounded-xl shadow-sm hover:bg-sandal-light transition-all flex items-center justify-center gap-3"
          >
            <Google className="w-5 h-5 text-red-500" />
            Sign in with Google
          </button>
        </div>
      </motion.div>
    </div>
  );
}
