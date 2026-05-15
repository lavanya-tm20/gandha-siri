import { motion } from 'motion/react';
import { BookOpen, FileText, Scale, ShieldCheck, ChevronRight, Download, Info } from 'lucide-react';
import { cn } from '../lib/utils';

const guides = [
  {
    title: 'Registration Process',
    icon: FileText,
    content: 'Farmers must register sandalwood trees with the Forest Department within 30 days of planting or discovery. Form 7 is the primary document required for legal notification.',
    color: 'bg-blue-500'
  },
  {
    title: 'Harvesting Guidelines',
    icon: Scale,
    content: 'Prior permission for harvest is mandatory. Section 84 of the Forest Act governs the extraction of heartwood. The government has first right of purchase for sandalwood heartwood.',
    color: 'bg-green-600'
  }
];

export default function LegalGuide() {
  return (
    <div className="space-y-8 max-w-4xl mx-auto pb-20">
      <div>
        <h1 className="text-3xl font-bold tracking-tight text-sandal-dark">Farmer's Legal Guide</h1>
        <p className="text-sandal-base font-medium">Navigating state regulations and sandalwood ownership laws.</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {guides.map((guide, idx) => (
          <motion.div 
            key={idx}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: idx * 0.1 }}
            className="glass-card p-6 flex items-start gap-5 hover:border-wood-primary/30 transition-all group"
          >
            <div className={cn("p-4 rounded-3xl text-white shadow-lg", guide.color)}>
              <guide.icon className="w-8 h-8" />
            </div>
            <div className="flex-1">
               <h3 className="text-xl font-bold text-sandal-dark mb-2 group-hover:text-wood-primary transition-colors">{guide.title}</h3>
               <p className="text-sm text-sandal-base leading-relaxed mb-4">{guide.content}</p>
               <button className="flex items-center gap-2 text-xs font-black uppercase tracking-widest text-wood-primary py-2 px-3 bg-sandal-light rounded-lg">
                  Read Full Protocol <ChevronRight className="w-4 h-4" />
               </button>
            </div>
          </motion.div>
        ))}
      </div>

      <section className="glass-card p-8 bg-wood-primary text-white relative overflow-hidden">
         <div className="relative z-10 w-full md:w-2/3">
            <h2 className="text-2xl font-black mb-4 tracking-tight">Forest Department Protection</h2>
            <p className="text-sm opacity-80 leading-relaxed mb-6">
              Sandalwood (Santalum album) is a highly regulated protected species. 
              The <em>National Sandalwood Development Program</em> provides subsidies and security support 
              for farmers maintaining more than 50 registered trees.
            </p>
            <div className="flex flex-wrap gap-4">
               <button className="bg-white text-wood-primary px-6 py-3 rounded-xl font-bold flex items-center gap-2 shadow-xl hover:scale-105 transition-transform">
                  <Download className="w-5 h-5" /> Download Act PDF
               </button>
               <button className="bg-white/10 backdrop-blur border border-white/20 text-white px-6 py-3 rounded-xl font-bold flex items-center gap-2">
                  <Info className="w-5 h-5" /> Local Offices
               </button>
            </div>
         </div>
         <BookOpen className="absolute -right-10 -bottom-10 w-64 h-64 opacity-5 rotate-12" />
      </section>

      <section>
         <h2 className="text-xs font-bold uppercase tracking-[0.2em] text-sandal-base mb-6">Safety Checklist</h2>
         <div className="space-y-3">
            {[
              "Ensure all tree tags match Department IDs",
              "Maintain digital mirror of paper receipts in Gandha-Siri App",
              "Report suspicious heartwood drilling immediately",
              "Review yearly growth certification requirements"
            ].map((item, idx) => (
              <div key={idx} className="glass-card p-5 flex items-center gap-4 bg-white/40">
                 <div className="w-8 h-8 rounded-full bg-green-500/10 flex items-center justify-center text-green-600">
                    <ShieldCheck className="w-5 h-5" />
                 </div>
                 <span className="font-bold text-sandal-dark text-sm">{item}</span>
              </div>
            ))}
         </div>
      </section>
    </div>
  );
}
