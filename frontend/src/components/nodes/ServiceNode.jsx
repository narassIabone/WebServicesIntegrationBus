import React from 'react';
import { Handle, Position, useReactFlow } from 'reactflow';
import { Database, Zap, Split, X } from 'lucide-react';
import { NODE_METADATA } from '../../constants/nodeMetadata.js';

const ServiceNode = ({ id, data, selected }) => {
    const isStart = data.config?.isStart;

    const { deleteElements } = useReactFlow();

    const onDelete = (e) => {
        e.stopPropagation();
        deleteElements({ nodes: [{ id }] });
    };

    const meta = NODE_METADATA[data.type] || {};
    const Icon = meta.icon;

    let borderColor = 'border-slate-200';
    if (selected) {
        borderColor = 'border-blue-500';
    } else if (isStart) {
        borderColor = 'border-emerald-500';
    }

    return (
        <div className={`
            group relative rounded-2xl bg-white border-2 transition-all duration-200
            ${borderColor}
            ${selected ? 'shadow-lg shadow-blue-100' : 'shadow-sm'}
            ${isStart ? 'shadow-lg shadow-emerald-100' : ''}
            hover:border-blue-300 hover:shadow-md
            w-24 h-24 flex flex-col items-center justify-center p-2
        `}>

            {/* Крестик удаления */}
            <button
                onClick={onDelete}
                className="absolute -top-1.5 -right-1.5 hidden group-hover:flex w-5 h-5 bg-slate-800 text-white rounded-full items-center justify-center shadow-md hover:bg-red-500 transition-colors z-50 border-2 border-white"
            >
                <X size={10} strokeWidth={4} />
            </button>

            {/* Хендлы */}
            <Handle type="source" position={Position.Top} id="h-top" className="!w-1.5 !h-1.5 !bg-slate-300 !border-white" isConnectable={true}/>
            <Handle type="source" position={Position.Bottom} id="h-bottom" className="!w-1.5 !h-1.5 !bg-slate-300 !border-white" isConnectable={true}/>
            <Handle type="source" position={Position.Left} id="h-left" className="!w-1.5 !h-1.5 !bg-slate-300 !border-white" isConnectable={true}/>
            <Handle type="source" position={Position.Right} id="h-right" className="!w-1.5 !h-1.5 !bg-slate-300 !border-white" isConnectable={true}/>

            {isStart && (
                <div className="absolute -top-3 left-1/2 -translate-x-1/2 bg-emerald-500 text-white text-[8px] font-black px-2 py-0.5 rounded-full shadow-sm z-10 animate-pulse">
                    START
                </div>
            )}

            {/* Динамический цвет ID */}
            <div className={`absolute top-2 left-2 text-[8px] font-black leading-none opacity-40 ${meta.accentColor || 'text-slate-300'}`}>
                #{id}
            </div>

            <div className="flex flex-col items-center justify-center gap-1.5">
                {/* Иконка с динамическим фоном и цветом */}
                <div className={`
                    p-2 rounded-xl transition-colors
                    ${meta.bgColor || 'bg-slate-50'} 
                    ${selected ? (meta.color || 'text-blue-500') : (meta.color || 'text-slate-600')}
                `}>
                    {Icon && <Icon size={20} fill={selected ? "currentColor" : "none"} fillOpacity={0.2} />}
                </div>

                {/* Заголовок с динамическим цветом текста */}
                <div className={`text-[10px] font-black uppercase tracking-tight text-center leading-tight ${meta.accentColor || 'text-slate-700'}`}>
                    {meta.title || data.type}
                </div>
            </div>
        </div>
    );
};

export default ServiceNode;