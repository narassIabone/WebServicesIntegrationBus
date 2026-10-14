import React from 'react';
import { Plus } from 'lucide-react';
import { NODE_METADATA } from '../../constants/nodeMetadata.js';

const NodeLibrary = ({ logic }) => {
    return (
        <div className="absolute left-6 top-6 z-10 flex flex-col gap-3 bg-white/90 backdrop-blur-md p-4 rounded-2xl shadow-2xl border border-slate-200 w-56">
            <h3 className="text-[10px] font-black text-slate-400 uppercase tracking-widest px-1 mb-1">
                Node Library
            </h3>

            {Object.entries(NODE_METADATA).map(([type, meta]) => {
                const Icon = meta.icon;
                // Получаем угол из метаданных, если он задан
                const rotation = meta.rotate || 0;

                return (
                    <button
                        key={type}
                        onClick={() => logic.addNode(type)}
                        className="flex items-center justify-between group px-4 py-3 bg-white border border-slate-100 rounded-xl hover:border-blue-500 hover:shadow-lg hover:shadow-blue-50 transition-all text-left"
                    >
                        <div className="flex items-center gap-3">
                            <div className={`
                                p-2 rounded-lg bg-slate-50 transition-colors 
                                group-hover:bg-blue-50 
                                ${meta.color || 'text-slate-600'}
                            `}>
                                {Icon && (
                                    <div style={{ transform: `rotate(${rotation}deg)`, display: 'flex' }}>
                                        <Icon size={16} />
                                    </div>
                                )}
                            </div>
                            <div>
                                <div className="text-[13px] font-bold text-slate-700 leading-none mb-1">
                                    {meta.title}
                                </div>
                                {meta.description && (
                                    <div className="text-[9px] text-slate-400 font-medium leading-tight">
                                        {meta.description}
                                    </div>
                                )}
                            </div>
                        </div>
                        <Plus
                            size={12}
                            className="text-slate-300 group-hover:text-blue-500 transition-colors shrink-0"
                        />
                    </button>
                );
            })}
        </div>
    );
};

export default NodeLibrary;