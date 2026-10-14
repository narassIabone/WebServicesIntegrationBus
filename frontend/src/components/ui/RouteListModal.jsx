import React from 'react';
import { Trash2, FolderOpen, X } from 'lucide-react';

const RouteListModal = ({ isOpen, onClose, routes, onSelect, onDelete }) => {
    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-[100] flex items-center justify-center bg-slate-900/60 backdrop-blur-sm animate-in fade-in duration-200">
            <div className="bg-white rounded-[32px] shadow-2xl w-[550px] max-h-[80vh] flex flex-col overflow-hidden border border-slate-100">

                {/* Шапка модалки */}
                <div className="p-8 border-b border-slate-50 flex justify-between items-center bg-slate-50/50">
                    <div>
                        <h2 className="text-2xl font-black text-slate-800 tracking-tight">REPOSITORY</h2>
                        <p className="text-slate-400 text-xs font-bold uppercase tracking-widest">Доступные маршруты в БД</p>
                    </div>
                    <button
                        onClick={onClose}
                        className="p-2 hover:bg-slate-100 rounded-full transition-colors text-slate-400"
                    >
                        <X size={20} />
                    </button>
                </div>

                {/* Список маршрутов */}
                <div className="flex-1 overflow-y-auto p-6 space-y-3 custom-scrollbar">
                    {routes.length === 0 ? (
                        <div className="text-center py-12 text-slate-400 italic">
                            Список пуст. Сохраните ваш первый маршрут!
                        </div>
                    ) : (
                        routes.map(route => (
                            <div
                                key={route.id}
                                className="flex items-center justify-between p-5 bg-white border border-slate-100 rounded-2xl hover:border-blue-200 hover:shadow-md transition-all group"
                            >
                                <div className="flex items-center gap-4">
                                    <div className="p-3 bg-blue-50 text-blue-500 rounded-xl">
                                        <FolderOpen size={20} />
                                    </div>
                                    <div>
                                        <div className="font-black text-slate-700 text-sm uppercase">{route.name}</div>
                                        <div className="text-[10px] text-slate-400 font-mono mt-1">{route.id}</div>
                                    </div>
                                </div>

                                <div className="flex items-center gap-2">
                                    <button
                                        onClick={() => { onSelect(route); onClose(); }}
                                        className="px-5 py-2.5 bg-slate-900 text-white rounded-xl text-[10px] font-black tracking-widest hover:bg-blue-600 transition-all uppercase"
                                    >
                                        LOAD
                                    </button>
                                    <button
                                        onClick={() => { if(window.confirm('Удалить маршрут?')) onDelete(route.id) }}
                                        className="p-2.5 text-slate-300 hover:text-red-500 hover:bg-red-50 rounded-xl transition-all"
                                    >
                                        <Trash2 size={18} />
                                    </button>
                                </div>
                            </div>
                        ))
                    )}
                </div>

                {/* Футер */}
                <div className="p-4 bg-slate-50 text-center text-[10px] text-slate-400 font-bold uppercase tracking-widest">
                    Total Routes: {routes.length}
                </div>
            </div>
        </div>
    );
};

export default RouteListModal;