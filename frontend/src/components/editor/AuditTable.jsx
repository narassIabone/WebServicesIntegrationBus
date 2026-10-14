import React, { useState } from 'react';
import { CheckCircle2, AlertCircle } from 'lucide-react';
import TraceDetailModal from '../ui/TraceDetailModal'; // Импортируем новый общий компонент

const AuditTable = ({ steps }) => {
    const [selectedStep, setSelectedStep] = useState(null);

    return (
        <div className="p-4 bg-slate-50/50 rounded-2xl border border-slate-100 my-2 shadow-inner">
            <table className="w-full text-left border-collapse">
                <thead>
                <tr className="text-[9px] font-bold text-slate-400 uppercase tracking-widest border-b border-slate-200">
                    <th className="pb-3 text-center w-12">Шаг</th>
                    <th className="pb-3 px-4 w-40">Узел</th>
                    <th className="pb-3 px-4">Действие</th>
                    <th className="pb-3 px-4">Данные</th>
                    <th className="pb-3 px-4 text-right">Время</th>
                </tr>
                </thead>
                <tbody className="relative">
                {steps.map((step, index) => (
                    <tr key={step.id} className="group transition-colors">
                        <td className="py-4 text-center relative">
                            {index !== steps.length - 1 && (
                                <div className="absolute left-1/2 top-10 bottom-0 w-px bg-slate-200 -translate-x-1/2" />
                            )}
                            <div className={`relative z-10 w-7 h-7 flex items-center justify-center rounded-full mx-auto font-black text-[10px] border-2 bg-white 
                                    ${step.status === 'SUCCESS' ? 'border-emerald-100 text-emerald-500' : 'border-rose-100 text-rose-500'}`}>
                                {index + 1}
                            </div>
                        </td>
                        <td className="py-4 px-4 overflow-hidden">
                            <div className="flex flex-col min-w-0">
                                <span className="px-1.5 py-0.5 bg-slate-200 text-slate-600 rounded text-[8px] font-black uppercase w-fit mb-0.5">
                                    {step.nodeType}
                                </span>
                                <div className="text-[10px] text-slate-700 flex items-center gap-1.5 min-w-0">
                                    <span className="font-mono font-medium text-slate-400 shrink-0">
                                        #{step.nodeId}
                                    </span>
                                    {step.nodeName ? (
                                        <span className="font-bold text-red-900 truncate" title={step.nodeName}>
                                            {step.nodeName}
                                        </span>
                                    ) : (
                                        <span className="font-bold text-slate-300 italic">
                                            Без имени
                                        </span>
                                    )}
                                </div>
                            </div>
                        </td>
                        <td className="py-4 px-4">
                            <div className="flex items-center gap-1.5">
                                {step.status === 'SUCCESS' ? <CheckCircle2 size={12} className="text-emerald-500" /> : <AlertCircle size={12} className="text-rose-500" />}
                                <span className="text-[10px] font-bold uppercase tracking-tight text-slate-600">{step.actionName}</span>
                            </div>
                        </td>
                        <td className="py-4 px-4">
                            <button onClick={() => setSelectedStep(step)} className="text-[9px] font-black uppercase text-blue-500 hover:underline">
                                Payload View
                            </button>
                        </td>
                        <td className="py-4 px-4 text-right">
                                <span className="text-[10px] font-mono text-slate-400 block">
                                    {new Date(step.createdAt).toLocaleTimeString(undefined, { hour12: false, fractionDigits: 3 })}
                                </span>
                            <span className="text-[9px] font-bold text-blue-400">+{step.executionTimeMs}ms</span>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>

            {/* Вызов общего модального окна */}
            <TraceDetailModal data={selectedStep} onClose={() => setSelectedStep(null)} />
        </div>
    );
};

export default AuditTable;