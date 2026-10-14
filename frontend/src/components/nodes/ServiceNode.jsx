import React from 'react';
import { Handle, Position, useReactFlow } from 'reactflow';
import { X } from 'lucide-react';
import { NODE_METADATA } from '../../constants/nodeMetadata.js';

const ServiceNode = ({ id, data, selected }) => {
    const isStart = data.config?.isStart;
    const nodeName = data.config?.name || data.name;

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
            w-28 h-24 flex flex-col items-center justify-center p-2
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

            {/* ИЗМЕНИЛИ ТУТ: Хедер карточки (ID + Тип узла в одну линию) */}
            <div className="absolute top-2 left-2 right-6 flex items-center gap-1.5 min-w-0 pointer-events-none">
                {/* ID узла */}
                <span className={`text-[8px] font-black leading-none opacity-40 shrink-0 ${meta.accentColor || 'text-slate-300'}`}>
                    #{id}
                </span>
                {/* Тип узла */}
                <span className="text-[7px] font-bold text-slate-400 uppercase tracking-wider truncate leading-none pt-[1px]">
                    {meta.title || data.type}
                </span>
            </div>

            {/* Контейнер контента */}
            <div className="flex flex-col items-center justify-center gap-1 w-full text-center mt-3">
                {/* Иконка */}
                <div className={`
                    p-1.5 rounded-xl transition-colors shrink-0
                    ${meta.bgColor || 'bg-slate-50'} 
                    ${selected ? (meta.color || 'text-blue-500') : (meta.color || 'text-slate-600')}
                `}>
                    {Icon && <Icon size={16} fill={selected ? "currentColor" : "none"} fillOpacity={0.2} />}
                </div>

                <div className="w-full px-1 min-w-0">
                    <div className="text-[10px] font-black uppercase tracking-tight whitespace-normal break-words leading-tight w-full">
                        {nodeName ? (
                            // Если имя введено — красим в сочный slate-900
                            <span className="text-rose-900">{nodeName}</span>
                        ) : (
                            // Если имени нет — показываем аккуратный серый плейсхолдер
                            <span className="text-slate-200 font-bold italic tracking-wide"></span>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ServiceNode;