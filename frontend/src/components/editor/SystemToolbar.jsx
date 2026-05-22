import React from 'react';
import { FolderOpen, Eraser, PlusCircle } from 'lucide-react';

const SystemToolbar = ({ logic, onOpenList }) => {
    const { setNodes, setLinks, setRouteName, setRouteId, setIsActive, saveToDb } = logic;

    const handleCreateNew = () => {
        if (window.confirm('Хотите сохранить текущие изменения перед созданием нового маршрута?')) {
            saveToDb();
        }
        setRouteId(null);
        setRouteName('NEW_ROUTE');
        setIsActive(false);
        setNodes([]);
        setLinks([]);
    };

    return (
        <div className="absolute top-6 right-6 z-10 flex items-center gap-2 bg-white/90 backdrop-blur-sm p-1.5 rounded-xl shadow-md border border-slate-200/60">
            <button
                onClick={handleCreateNew}
                className="p-2.5 bg-slate-800 text-white rounded-lg hover:bg-black transition-all group"
                title="New Route"
            >
                <PlusCircle size={18} className="group-hover:rotate-90 transition-transform" />
            </button>

            <div className="w-[1px] h-5 bg-slate-200 mx-0.5" />

            <button
                onClick={onOpenList}
                className="p-2.5 bg-white border border-slate-100 text-slate-500 rounded-lg hover:text-blue-600 hover:border-blue-200 transition-all"
                title="Load Route"
            >
                <FolderOpen size={18} />
            </button>

            <button
                onClick={() => { if(window.confirm('Очистить холст?')) { setNodes([]); setLinks([]); }}}
                className="p-2.5 bg-white border border-slate-100 text-slate-400 rounded-lg hover:text-orange-500 hover:border-orange-200 transition-all"
                title="Clear Canvas"
            >
                <Eraser size={18} />
            </button>
        </div>
    );
};

export default SystemToolbar;