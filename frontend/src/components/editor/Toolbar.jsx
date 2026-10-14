import React from 'react';
import { Save, Play, Power } from 'lucide-react';

const Toolbar = ({ logic }) => {
    const { isActive, setIsActive, setRouteName, routeName } = logic;

    return (
        <div className="absolute bottom-10 left-1/2 -translate-x-1/2 z-10 flex items-center gap-2 bg-white/95 backdrop-blur-md p-1.5 rounded-2xl shadow-xl border border-slate-200">
            <div className="flex items-center gap-3 px-4 py-1 border-r border-slate-100">
                <div className={`w-2 h-2 rounded-full transition-all duration-500 ${isActive ? 'bg-green-500 animate-pulse' : 'bg-slate-300'}`} />
                <input
                    value={routeName}
                    onChange={(e) => setRouteName(e.target.value)}
                    className="bg-transparent border-none font-black text-slate-700 focus:ring-0 text-[13px] w-40 uppercase tracking-tight"
                    placeholder="ROUTE_NAME"
                />
            </div>

            <div className="flex items-center gap-1">
                <div className="w-[1px] h-6 bg-slate-100 mx-1" />
                <button
                    onClick={logic.saveToDb}
                    className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-xl hover:bg-blue-700 transition-all text-[11px] font-black tracking-widest group"
                >
                    <Save size={14} className="group-hover:scale-110 transition-transform" />
                    SAVE TO DB
                </button>
            </div>
        </div>
    );
};

export default Toolbar;