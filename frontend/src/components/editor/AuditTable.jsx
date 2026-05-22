import React, { useState } from 'react';
import { CheckCircle2, AlertCircle, Clock, ChevronRight, X, Fingerprint, Code2, Database, ArrowDown } from 'lucide-react';
import JsonBlock from '../ui/JsonBlock.jsx';

const AuditTable = ({ steps }) => {
    const [selectedStep, setSelectedStep] = useState(null);

    return (
        <div className="p-4 bg-slate-50/50 rounded-2xl border border-slate-100 my-2 shadow-inner">
            <table className="w-full text-left border-collapse">
                <thead>
                <tr className="text-[9px] font-bold text-slate-400 uppercase tracking-widest border-b border-slate-200">
                    <th className="pb-3 text-center w-12">Шаг</th>
                    <th className="pb-3 px-4">Узел</th>
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
                        <td className="py-4 px-4 whitespace-nowrap">
                            <div className="flex flex-col">
                                <span className="px-1.5 py-0.5 bg-slate-200 text-slate-600 rounded text-[8px] font-black uppercase w-fit mb-0.5">{step.nodeType}</span>
                                <span className="text-[10px] font-bold text-slate-700">{step.nodeId}</span>
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

            {/* Модалка для сравнения (из твоего предыдущего дизайна) */}
            {selectedStep && (
                <div className="fixed inset-0 z-[100] flex items-center justify-center p-6 bg-slate-900/60 backdrop-blur-sm">
                    <div className="relative bg-white rounded-[3rem] shadow-2xl w-full max-w-6xl max-h-[90vh] overflow-hidden flex flex-col">
                        <div className="px-10 py-6 border-b border-gray-100 flex justify-between items-center bg-white">
                            <h3 className="text-xl font-black text-gray-800 uppercase tracking-tight">Step Detail: {selectedStep.nodeId}</h3>
                            <button onClick={() => setSelectedStep(null)} className="p-2 hover:bg-gray-100 rounded-full transition-all"><X size={24} className="text-gray-400" /></button>
                        </div>
                        <div className="p-10 overflow-y-auto bg-slate-50/30 flex-1 space-y-6">
                            <section className="flex flex-col bg-indigo-50/50 p-6 rounded-3xl border border-indigo-100 relative">
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-6 h-[350px]">
                                    <JsonBlock title="Input" data={selectedStep.payloadBefore} />
                                    <JsonBlock title="Output" data={selectedStep.payloadAfter} />
                                </div>
                            </section>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default AuditTable;