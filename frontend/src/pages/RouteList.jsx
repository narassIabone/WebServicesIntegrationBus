import React, { useState, useEffect } from 'react';
import {
    Play, Pause, Trash2, Edit3, Plus,
    Search, Activity, Zap, Copy, RotateCw,
    AlertCircle
} from 'lucide-react';
import { toast } from 'react-hot-toast';

const RouteList = ({ onEditRoute, onCreateNew }) => {
    const [routes, setRoutes] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [loading, setLoading] = useState(true);
    const [isRefreshing, setIsRefreshing] = useState(false);

    const fetchRoutes = async () => {
        setLoading(true);
        try {
            const response = await fetch('/api/admin/routes');
            if (!response.ok) throw new Error();
            const data = await response.json();
            setRoutes(data);
        } catch (error) {
            toast.error("Не удалось загрузить маршруты");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchRoutes();
    }, []);

    const toggleStatus = async (id, currentStatus) => {
        try {
            const response = await fetch(`/api/admin/routes/${id}/status?active=${!currentStatus}`, {
                method: 'PATCH'
            });

            if (response.ok) {
                setRoutes(prev => prev.map(r =>
                    r.id === id ? { ...r, active: !currentStatus } : r
                ));
                toast.success(`Маршрут ${!currentStatus ? 'запущен' : 'остановлен'}`);
            }
        } catch (error) {
            toast.error("Ошибка при смене статуса");
        }
    };

    const deleteRoute = async (id) => {
        if (!window.confirm("Удалить этот маршрут? Это действие нельзя отменить.")) return;

        try {
            const response = await fetch(`/api/admin/routes/${id}`, { method: 'DELETE' });
            if (response.ok) {
                setRoutes(prev => prev.filter(r => r.id !== id));
                toast.success("Маршрут удален");
            }
        } catch (error) {
            toast.error("Ошибка при удалении");
        }
    };

    const handleRefreshCache = async () => {
        setIsRefreshing(true);
        try {
            const response = await fetch('/api/admin/routes/cache/refresh', { method: 'POST' });
            if (response.ok) toast.success("Кэш шины успешно обновлен");
        } catch (error) {
            toast.error("Ошибка обновления кэша");
        } finally {
            setIsRefreshing(false);
        }
    };

    const filteredRoutes = routes.filter(r =>
        r.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        r.id?.toLowerCase().includes(searchTerm.toLowerCase())
    );

    return (
        <div className="max-w-7xl mx-auto space-y-8 animate-in fade-in duration-500">

            {/* HEADER SECTION */}
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                <div>
                    <h1 className="text-3xl font-black text-slate-800 tracking-tight flex items-center gap-3">
                        <Activity className="text-blue-600" size={32} />
                        ROUTE MANAGER
                    </h1>
                    <p className="text-[11px] font-black text-slate-400 uppercase tracking-[0.2em] mt-1">
                        Управление узлами интеграционной шины ESB
                    </p>
                </div>

                <div className="flex items-center gap-3">
                    <button
                        onClick={handleRefreshCache}
                        disabled={isRefreshing}
                        className="p-3 text-slate-400 hover:text-blue-600 hover:bg-blue-50 rounded-2xl transition-all"
                        title="Обновить кэш шины"
                    >
                        <RotateCw size={20} className={isRefreshing ? 'animate-spin' : ''} />
                    </button>
                    <button
                        onClick={onCreateNew}
                        className="flex items-center gap-2 px-6 py-3 bg-blue-600 text-white rounded-[1.5rem] font-bold text-sm hover:bg-blue-700 transition-all shadow-lg shadow-blue-200 active:scale-95"
                    >
                        <Plus size={18} strokeWidth={3} /> СОЗДАТЬ МАРШРУТ
                    </button>
                </div>
            </div>

            {/* SEARCH & FILTERS */}
            <div className="bg-white p-4 rounded-[2rem] shadow-sm border border-slate-100 flex items-center gap-4">
                <div className="relative flex-1">
                    <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-300" size={18} />
                    <input
                        type="text"
                        placeholder="Поиск по названию или UUID..."
                        className="w-full pl-12 pr-4 py-3 bg-slate-50 border-none rounded-2xl text-sm font-bold text-slate-700 focus:ring-2 focus:ring-blue-500/10 transition-all placeholder:text-slate-300"
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>
                <div className="px-4 py-2 bg-blue-50 rounded-xl text-[10px] font-black text-blue-600 uppercase tracking-widest shrink-0">
                    Найдено: {filteredRoutes.length}
                </div>
            </div>

            {/* LIST */}
            <div className="grid grid-cols-1 gap-3">
                {loading ? (
                    <div className="py-24 text-center font-black text-slate-200 text-2xl animate-pulse tracking-tighter uppercase">
                        Scanning Infrastructure...
                    </div>
                ) : filteredRoutes.length > 0 ? (
                    filteredRoutes.map(route => (
                        <div
                            key={route.id}
                            className={`group bg-white border rounded-[2.2rem] p-5 flex flex-col md:flex-row md:items-center gap-6 transition-all hover:shadow-xl hover:shadow-slate-100/50 
                                ${route.active ? 'border-slate-100' : 'border-slate-50 opacity-70'}`}
                        >
                            <div className={`w-14 h-14 rounded-[1.4rem] flex items-center justify-center shrink-0 transition-colors
                                ${route.active ? 'bg-blue-600 text-white shadow-lg shadow-blue-100' : 'bg-slate-100 text-slate-400'}`}>
                                {route.active  ? <Zap size={24} fill="currentColor" /> : <Pause size={24} fill="currentColor" />}
                            </div>

                            <div className="flex-1 min-w-0">
                                <div className="flex items-center gap-3 mb-1.5">
                                    <h3 className="text-base font-black text-slate-800 truncate uppercase tracking-tight">
                                        {route.name || "Unnamed_Route_Service"}
                                    </h3>
                                    <span className={`px-2 py-0.5 rounded-lg text-[9px] font-black uppercase tracking-widest
                                        ${route.active ? 'bg-emerald-100 text-emerald-600' : 'bg-slate-100 text-slate-400'}`}>
                                        {route.active? 'Online' : 'Offline'}
                                    </span>
                                </div>
                                <div className="flex flex-wrap items-center gap-x-5 gap-y-1">
                                    <div className="flex items-center gap-1.5 text-[10px] font-mono font-bold text-slate-400 uppercase truncate">
                                        <span className="text-blue-500/50 shrink-0">UUID:</span> {route.id}
                                    </div>
                                    <div className="flex items-center gap-1.5 text-[10px] font-black text-slate-400 uppercase tracking-tighter shrink-0">
                                        <div className="w-1 h-1 rounded-full bg-slate-300" />
                                        Nodes: {route.nodes?.length || 0}
                                    </div>
                                </div>
                            </div>

                            <div className="flex items-center gap-2 bg-slate-50/50 p-1.5 rounded-[1.4rem] self-end md:self-center">
                                <button
                                    onClick={() => toggleStatus(route.id, route.active)}
                                    className={`p-3 rounded-xl transition-all ${route.active ? 'hover:bg-amber-100 text-amber-600' : 'hover:bg-emerald-100 text-emerald-600'}`}
                                >
                                    {route.active ? <Pause size={20} strokeWidth={2.5} /> : <Play size={20} strokeWidth={2.5} />}
                                </button>

                                <button
                                    onClick={() => onEditRoute(route.id)}
                                    className="p-3 rounded-xl hover:bg-blue-100 text-blue-600 transition-all"
                                >
                                    <Edit3 size={20} strokeWidth={2.5} />
                                </button>

                                <div className="w-px h-6 bg-slate-200 mx-1" />

                                <button
                                    onClick={() => deleteRoute(route.id)}
                                    className="p-3 rounded-xl hover:bg-rose-100 text-rose-500 transition-all"
                                >
                                    <Trash2 size={20} />
                                </button>
                            </div>
                        </div>
                    ))
                ) : (
                    <div className="py-20 flex flex-col items-center justify-center border-2 border-dashed border-slate-100 rounded-[3rem]">
                        <AlertCircle size={48} className="text-slate-200 mb-4" />
                        <span className="text-sm font-black text-slate-300 uppercase tracking-widest">Маршруты не обнаружены</span>
                    </div>
                )}
            </div>
        </div>
    );
};

export default RouteList;