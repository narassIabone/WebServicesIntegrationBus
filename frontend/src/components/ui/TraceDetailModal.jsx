import React from 'react';
import { X, Fingerprint, Code2, Database, ArrowDown, Settings, Activity } from 'lucide-react';
import JsonBlock from './JsonBlock.jsx';

const TraceDetailModal = ({ data, onClose }) => {
    if (!data) return null;

    const isSuccess = data.status === 'SUCCESS';

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm transition-opacity">
            <style>{`
                .scrollbar-thin::-webkit-scrollbar { width: 4px; height: 4px; }
                .scrollbar-thin::-webkit-scrollbar-thumb { background: #334155; border-radius: 10px; }
                .scrollbar-thin::-webkit-scrollbar-track { background: transparent; }
            `}</style>

            <div className="relative bg-white rounded-[2.5rem] shadow-2xl w-full max-w-6xl max-h-[96vh] overflow-hidden flex flex-col animate-in zoom-in-95 duration-200">

                {/* Header */}
                <div className="px-10 py-6 pb-2 border-b border-gray-100 flex justify-between items-center bg-white">
                    <div className="space-y-1">
                        <div className="flex items-center gap-3">
                            <span className={`px-2.5 py-0.5 text-white text-[10px] font-bold rounded-full uppercase tracking-widest ${
                                isSuccess ? 'bg-emerald-500' : 'bg-rose-500'
                            }`}>
                                {isSuccess ? 'Step Analysis' : 'Incident Analysis'}
                            </span>
                            {data.messageId && (
                                <div className="flex items-center gap-1.5 text-gray-400">
                                    <Fingerprint size={14} />
                                    <span className="text-[11px] font-mono uppercase tracking-widest">Msg ID: {data.messageId}</span>
                                </div>
                            )}
                        </div>
                        <h3 className="text-2xl font-black text-gray-800 tracking-tight uppercase tracking-wider">
                            {data.routeName || `Node: ${data.nodeType}`}
                        </h3>
                    </div>
                    <button onClick={onClose} className="p-3 hover:bg-gray-100 rounded-full transition-all group">
                        <X size={28} className="text-gray-400 group-hover:rotate-90 duration-200" />
                    </button>
                </div>

                {/* Content Layered */}
                <div className="p-10 overflow-y-auto bg-slate-50/30 flex-1 space-y-5">

                    {/* РЯД 1: Стек-трейс ошибки (Рендерится только при наличии ошибки) */}
                    {data.errorMessage && (
                        <section className="bg-white p-6 pt-0 rounded-2xl border border-gray-100 shadow-sm space-y-3">
                            <h4 className="text-[10px] font-bold text-rose-400 uppercase flex items-center gap-2 tracking-[0.2em]">
                                <div className="w-1.5 h-1.5 bg-rose-500 rounded-full animate-pulse" /> Стек-трейс ошибки
                            </h4>
                            <div className="p-5 bg-rose-50/50 rounded-xl text-[13px] text-rose-700 font-mono leading-relaxed border border-rose-100 max-h-48 overflow-y-auto scrollbar-thin italic">
                                {data.errorMessage}
                            </div>
                        </section>
                    )}

                    {/* РЯД 2: Свойства и Конфиг */}
                    <section className="grid grid-cols-1 lg:grid-cols-12 gap-6">
                        <div className="lg:col-span-3 bg-white p-6 rounded-2xl border border-gray-100 shadow-sm space-y-5">
                            <h4 className="text-[10px] font-bold text-gray-400 uppercase flex items-center gap-2">
                                <Activity size={14} className="text-blue-500"/> Свойства узла
                            </h4>
                            <div className="grid grid-cols-2 lg:grid-cols-1 gap-4">
                                <div className="flex flex-col">
                                    <span className="text-[10px] text-gray-400 uppercase">Type</span>
                                    <span className="text-xs font-bold bg-gray-100 self-start px-2 rounded mt-1 uppercase leading-5">{data.nodeType}</span>
                                </div>
                                <div className="flex flex-col min-w-0">
                                    <span className="text-[10px] text-gray-400 uppercase">Node Name</span>
                                    <span className="text-xs font-bold text-red-900 mt-1 truncate" title={data.nodeName || 'Без имени'}>
                                        {data.nodeName || '—'}
                                    </span>
                                </div>
                                <div className="flex flex-col">
                                    <span className="text-[10px] text-gray-400 uppercase">Node ID</span>
                                    <span className="text-xs font-bold text-gray-700 mt-1 uppercase tracking-widest">#{data.nodeId}</span>
                                </div>
                                <div className="flex flex-col">
                                    <span className="text-[10px] text-gray-400 uppercase">Latency</span>
                                    <span className="text-xs font-bold text-blue-600 mt-1 uppercase tracking-widest">{data.executionTimeMs} ms</span>
                                </div>
                            </div>
                        </div>
                        <div className="lg:col-span-9 h-64 bg-white p-2 rounded-2xl border border-gray-100 shadow-sm">
                            <JsonBlock title="Конфигурация (Node Config)" data={data.nodeConfig || {}} icon={Settings} />
                        </div>
                    </section>

                    {/* РЯД 3: Трансформация данных */}
                    <section className="flex flex-col bg-indigo-50/50 p-8 rounded-[3rem] border border-indigo-100 relative">
                        <div className="flex items-center justify-between mb-6">
                            <h4 className="text-[10px] font-bold text-indigo-400 uppercase tracking-[0.3em]">Трансформация данных</h4>
                            <div className="text-[10px] text-indigo-300 font-mono italic">In / Out Comparison</div>
                        </div>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-8 h-[350px] relative z-10">
                            <JsonBlock title="Payload Before (Вход)" data={data.payloadBefore} />

                            <div className="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 hidden md:flex items-center justify-center w-12 h-12 bg-white rounded-full border border-indigo-100 text-indigo-400 shadow-md z-20">
                                <ArrowDown size={24} className="-rotate-90" />
                            </div>

                            <JsonBlock title="Payload After (Выход)" data={data.payloadAfter} />
                        </div>
                    </section>

                    {/* РЯД 4: Метаданные */}
                    <section className="bg-blue-50/30 p-6 rounded-2xl border border-blue-100/50">
                        <h4 className="text-[10px] font-bold text-blue-400 uppercase tracking-widest mb-4">Окружение (Metadata)</h4>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                            <JsonBlock title="Context" data={data.contextSnapshot || {}} icon={Code2} height="h-64" />
                            <JsonBlock title="Headers" data={data.headersSnapshot || {}} icon={Database} height="h-64" />
                        </div>
                    </section>
                </div>

                {/* Footer (Trace Info) */}
                <div className="px-10 py-4 bg-gray-50/80 border-t border-gray-100 flex justify-center items-center">
                    <span className="text-[10px] text-gray-400 font-mono tracking-widest uppercase italic">
                        Technical Trace: {data.traceId || 'N/A'}
                    </span>
                </div>
            </div>
        </div>
    );
};

export default TraceDetailModal;