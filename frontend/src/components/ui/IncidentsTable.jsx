import React, { useState } from 'react';
import { X, Settings, Database, Code2, Fingerprint, Activity, ArrowDown } from 'lucide-react';
import JsonBlock from './JsonBlock';

const IncidentsTable = ({ incidents }) => {
    const [selectedItem, setSelectedItem] = useState(null);

    return (
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
            <style>{`
                .scrollbar-thin::-webkit-scrollbar { width: 4px; height: 4px; }
                .scrollbar-thin::-webkit-scrollbar-thumb { background: #334155; border-radius: 10px; }
                .scrollbar-thin::-webkit-scrollbar-track { background: transparent; }
            `}</style>

            {/* Таблица инцидентов */}
            <div className="overflow-x-auto">
                <table className="w-full text-left border-collapse">
                    <thead>
                    <tr className="bg-gray-50/50 border-b border-gray-100 text-[10px] font-bold text-gray-400 uppercase">
                        <th className="p-4 uppercase">Трассировка / Маршрут</th>
                        <th className="p-4 w-1/5 pl-6 uppercase">Узел</th>
                        <th className="p-4 w-1/3 uppercase">Ошибка</th>
                        <th className="p-4 text-right uppercase">Дата / Время</th>
                    </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-50">
                    {incidents.map((item) => (
                        <tr key={item.id} onClick={() => setSelectedItem(item)} className="hover:bg-blue-50/40 cursor-pointer transition-colors group text-xs">
                            <td className="p-4">
                                    <span className="text-[10px] font-mono bg-blue-50 text-blue-600 px-2 py-0.5 rounded-md mb-1 inline-block uppercase">
                                        {item.traceId?.substring(0, 8)}...
                                    </span>
                                <div className="font-bold text-gray-700 uppercase">
                                    {item.routeName || 'Default Route'}
                                </div>
                            </td>
                            <td className="p-4">
                                <div>
                                    <span className="px-2 py-0.5 bg-gray-100 text-gray-500 rounded text-[9px] font-bold uppercase">
                                        {item.nodeType}
                                    </span>
                                    <div className="text-[10px] text-gray-400 ml-3 mt-2 uppercase tracking-tighter">
                                        ID: {item.nodeId}
                                    </div>
                                </div>
                            </td>
                            <td className="p-4 text-rose-600 font-medium line-clamp-2">{item.errorMessage}</td>
                            <td className="p-4 text-right">
                                <div className="text-[10px] text-gray-400">{new Date(item.createdAt).toLocaleDateString()}</div>
                                <div className="font-bold text-gray-900">{new Date(item.createdAt).toLocaleTimeString()}</div>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>

            {/* --- МОДАЛЬНОЕ ОКНО --- */}
            {selectedItem && (
                <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm transition-opacity">
                    <div className="relative bg-white rounded-[2.5rem] shadow-2xl w-full max-w-6xl max-h-[96vh] overflow-hidden flex flex-col animate-in zoom-in-95 duration-200">

                        {/* Header */}
                        <div className="px-10 py-6 pb-2 border-b border-gray-100 flex justify-between items-center bg-white">
                            <div className="space-y-1">
                                <div className="flex items-center gap-3">
                                    <span className="px-2.5 py-0.5 bg-rose-500 text-white text-[10px] font-bold rounded-full uppercase tracking-widest">Incident Analysis</span>
                                    <div className="flex items-center gap-1.5 text-gray-400">
                                        <Fingerprint size={14} />
                                        <span className="text-[11px] font-mono uppercase tracking-tighter tracking-widest">Msg ID: {selectedItem.messageId}</span>
                                    </div>
                                </div>
                                <h3 className="text-2xl font-black text-gray-800 tracking-tight uppercase tracking-wider">{selectedItem.routeName}</h3>
                            </div>
                            <button onClick={() => setSelectedItem(null)} className="p-3 hover:bg-gray-100 rounded-full transition-all group">
                                <X size={28} className="text-gray-400 group-hover:rotate-90 duration-200" />
                            </button>
                        </div>

                        {/* Content Layered */}
                        <div className="p-10 overflow-y-auto bg-slate-50/30 flex-1 space-y-5">

                            {/* РЯД 1: Стек-трейс ошибки */}
                            <section className="bg-white p-6 pt-0 rounded-2xl border border-gray-100 shadow-sm space-y-3">
                                <h4 className="text-[10px] font-bold text-rose-400 uppercase flex items-center gap-2 tracking-[0.2em]">
                                    <div className="w-1.5 h-1.5 bg-rose-500 rounded-full animate-pulse" /> Стек-трейс ошибки
                                </h4>
                                <div className="p-5 bg-rose-50/50 rounded-xl text-[13px] text-rose-700 font-mono leading-relaxed border border-rose-100 max-h-48 overflow-y-auto scrollbar-thin italic">
                                    {selectedItem.errorMessage}
                                </div>
                            </section>

                            {/* РЯД 2: Свойства и Конфиг */}
                            <section className="grid grid-cols-1 lg:grid-cols-12 gap-6">
                                <div className="lg:col-span-3 bg-white p-6 rounded-2xl border border-gray-100 shadow-sm space-y-5">
                                    <h4 className="text-[10px] font-bold text-gray-400 uppercase flex items-center gap-2"><Activity size={14} className="text-blue-500"/> Свойства узла</h4>
                                    <div className="grid grid-cols-2 lg:grid-cols-1 gap-4">
                                        <div className="flex flex-col"><span className="text-[10px] text-gray-400 uppercase">Type</span><span className="text-xs font-bold bg-gray-100 self-start px-2 rounded mt-1 uppercase leading-5">{selectedItem.nodeType}</span></div>
                                        <div className="flex flex-col"><span className="text-[10px] text-gray-400 uppercase">Node ID</span><span className="text-xs font-bold text-gray-700 mt-1 uppercase tracking-widest">{selectedItem.nodeId}</span></div>
                                        <div className="flex flex-col"><span className="text-[10px] text-gray-400 uppercase">Latency</span><span className="text-xs font-bold text-blue-600 mt-1 uppercase tracking-widest">{selectedItem.executionTimeMs} ms</span></div>
                                    </div>
                                </div>
                                <div className="lg:col-span-9 h-64 bg-white p-2 rounded-2xl border border-gray-100 shadow-sm">
                                    <JsonBlock title="Конфигурация (Node Config)" data={selectedItem.nodeConfig || {}} icon={Settings} />
                                </div>
                            </section>

                            {/* РЯД 3: Трансформация данных (теперь в стиле Indigo) */}
                            <section className="flex flex-col bg-indigo-50/50 p-8 rounded-[3rem] border border-indigo-100 relative">
                                <div className="flex items-center justify-between mb-6">
                                    <h4 className="text-[10px] font-bold text-indigo-400 uppercase tracking-[0.3em]">Трансформация данных</h4>
                                    <div className="text-[10px] text-indigo-300 font-mono italic">In / Out Comparison</div>
                                </div>
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-8 h-[350px] relative z-10">
                                    <JsonBlock title="Payload Before (Вход)" data={selectedItem.payloadBefore} />

                                    <div className="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 hidden md:flex items-center justify-center w-12 h-12 bg-white rounded-full border border-indigo-100 text-indigo-400 shadow-md z-20">
                                        <ArrowDown size={24} className="-rotate-90" />
                                    </div>

                                    <JsonBlock title="Payload After (Выход)" data={selectedItem.payloadAfter} />
                                </div>
                            </section>

                            {/* РЯД 4: Метаданные (теперь в стиле Blue) */}
                            <section className="bg-blue-50/30 p-6 rounded-2xl border border-blue-100/50">
                                <h4 className="text-[10px] font-bold text-blue-400 uppercase tracking-widest mb-4">Окружение (Metadata)</h4>
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                    <JsonBlock title="Context" data={selectedItem.contextSnapshot} icon={Code2} height="h-64" />
                                    <JsonBlock title="Headers" data={selectedItem.headersSnapshot} icon={Database} height="h-64" />
                                </div>
                            </section>


                        </div>

                        {/* Footer (Trace Info) */}
                        <div className="px-10 py-4 bg-gray-50/80 border-t border-gray-100 flex justify-center items-center">
                            <span className="text-[10px] text-gray-400 font-mono tracking-widest uppercase italic">
                                Technical Trace: {selectedItem.traceId}
                            </span>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default IncidentsTable;