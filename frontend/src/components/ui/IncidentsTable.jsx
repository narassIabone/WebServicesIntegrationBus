import React, { useState } from 'react';
import { CheckCircle } from 'lucide-react';
import TraceDetailModal from './TraceDetailModal'; // Импортируем новый общий компонент

const IncidentsTable = ({ incidents }) => {
    const [selectedItem, setSelectedItem] = useState(null);

    return (
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
            {incidents && incidents.length > 0 ? (
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
                                <td className="p-4 overflow-hidden">
                                    <div className="flex flex-col min-w-0">
                                        <span className="px-2 py-0.5 bg-gray-100 text-gray-500 rounded text-[9px] font-bold uppercase w-fit mb-1.5">
                                            {item.nodeType}
                                        </span>
                                        <div className="text-[10px] text-gray-700 flex items-center gap-1.5 min-w-0">
                                            <span className="font-mono font-medium text-gray-400 shrink-0">
                                                #{item.nodeId}
                                            </span>
                                            {item.nodeName ? (
                                                <span className="font-bold text-red-900 truncate" title={item.nodeName}>
                                                    {item.nodeName}
                                                </span>
                                            ) : (
                                                <span className="font-bold text-slate-300 italic">
                                                    Без имени
                                                </span>
                                            )}
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
            ) : (
                <div className="flex flex-col items-center justify-center py-16 text-slate-400 space-y-2 animate-in fade-in duration-300">
                    <div className="p-3 bg-emerald-50 rounded-full text-emerald-500 mb-1">
                        <CheckCircle size={28} />
                    </div>
                    <p className="text-sm font-semibold text-slate-700">Инцидентов не обнаружено</p>
                    <p className="text-xs text-slate-400 max-w-sm text-center">
                        Шина работает стабильно. За выбранный промежуток времени критических ошибок маршрутизации не зафиксировано.
                    </p>
                </div>
            )}

            {/* Вызов общего модального окна */}
            <TraceDetailModal data={selectedItem} onClose={() => setSelectedItem(null)} />
        </div>
    );
};

export default IncidentsTable;