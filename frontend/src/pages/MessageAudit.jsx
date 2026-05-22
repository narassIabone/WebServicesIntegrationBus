import React, { useState, useEffect } from 'react';
import { Search, Filter, Calendar, ChevronDown, ArrowRight, MessageSquare, ChevronLeft, ChevronRight } from 'lucide-react';
import { toast } from 'react-hot-toast';
import AuditTable from '../components/editor/AuditTable';
import CustomSelect from '../components/ui/CustomSelect';

const MessageAudit = () => {
    const [messages, setMessages] = useState([]);
    const [loading, setLoading] = useState(true);
    const [expandedId, setExpandedId] = useState(null);
    const [traceSteps, setTraceSteps] = useState({});
    const [searchTerm, setSearchTerm] = useState('');
    const [routeFilter, setRouteFilter] = useState('ALL');
    const [dateFilter, setDateFilter] = useState({ start: '', end: '' });
    const [totalCount, setTotalCount] = useState(0);
    const [currentPage, setCurrentPage] = useState(1);
    const recordsPerPage = 10;
    const [allRoutes, setAllRoutes] = useState(['ALL']);

    const fetchMessages = async () => {
        setLoading(true);
        try {
            const params = new URLSearchParams({
                page: currentPage,
                limit: recordsPerPage,
                route: routeFilter,
                start: dateFilter.start || '',
                end: dateFilter.end || '',
                search: searchTerm || ''
            });

            const response = await fetch(`/api/admin/audit/messages?${params.toString()}`);
            if (!response.ok) throw new Error('Ошибка при загрузке сообщений');

            const data = await response.json();

            setMessages(data.messages || []);
            setTotalCount(data.totalCount || 0);

            if (allRoutes.length <= 1 && data.messages) {
                const routes = data.messages.map(m => m.routeName).filter(Boolean);
                setAllRoutes(['ALL', ...new Set(routes)]);
            }
        } catch (error) {
            toast.error(error.message);
        } finally {
            setLoading(false);
        }
    };

    const fetchTraceDetails = async (traceId) => {
        if (traceSteps[traceId]) return;
        try {
            const response = await fetch(`/api/admin/audit/trace/${traceId}`);
            if (!response.ok) throw new Error('Не удалось загрузить детали шагов');
            const data = await response.json();
            setTraceSteps(prev => ({ ...prev, [traceId]: data }));
        } catch (error) {
            toast.error(error.message);
        }
    };

    useEffect(() => {
        fetchMessages();
    }, [searchTerm, routeFilter, dateFilter, currentPage]);

    // Сброс на первую страницу при изменении фильтров
    useEffect(() => {
        setCurrentPage(1);
    }, [searchTerm, routeFilter, dateFilter]);

    // Выборка записей для текущей страницы
    const paginatedMessages = messages;

    const totalPages = Math.ceil(totalCount / recordsPerPage);

    const toggleRow = (id) => {
        const isExpanding = expandedId !== id;
        setExpandedId(isExpanding ? id : null);
        if (isExpanding && id) {
            fetchTraceDetails(id);
        }
    };

    return (
        <div className="space-y-6">
            {/* ФИЛЬТРЫ */}
            <div className="grid grid-cols-1 md:grid-cols-5 gap-4 bg-white p-6 rounded-[2rem] shadow-sm border border-gray-100">

                {/* Поиск */}
                <div className="relative md:col-span-1">
                    <Search className="absolute left-3 top-3 text-gray-400" size={18} />
                    <input
                        type="text" placeholder="Trace ID..."
                        className="w-full pl-10 pr-4 py-2.5 bg-slate-50 border-none rounded-xl text-sm font-mono focus:ring-2 focus:ring-blue-500/20"
                        value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>

                {/* Выбор маршрута - используем allRoutes вместо uniqueRoutes */}
                <div className="md:col-span-1">
                    <CustomSelect
                        value={routeFilter}
                        onChange={setRouteFilter}
                        options={allRoutes}
                        placeholder="Все маршруты"
                        icon={Filter}
                    />
                </div>

                {/* Блок дат в ряд с подписями */}
                <div className="md:col-span-2 flex items-center gap-4 bg-slate-50 px-4 py-2 rounded-xl">
                    <Calendar className="text-gray-400 shrink-0" size={18} />

                    <div className="flex flex-1 items-center gap-3">
                        <div className="flex flex-col flex-1">
                            <span className="text-[9px] uppercase font-black text-slate-400 mb-1 ml-1">От</span>
                            <input
                                type="datetime-local"
                                className="bg-transparent border-none p-0 text-[11px] font-bold text-gray-600 focus:ring-0 cursor-pointer"
                                value={dateFilter.start}
                                onChange={(e) => setDateFilter(prev => ({ ...prev, start: e.target.value }))}
                            />
                        </div>

                        <div className="w-px h-8 bg-gray-200" /> {/* Разделитель */}

                        <div className="flex flex-col flex-1">
                            <span className="text-[9px] uppercase font-black text-slate-400 mb-1 ml-1">До</span>
                            <input
                                type="datetime-local"
                                className="bg-transparent border-none p-0 text-[11px] font-bold text-gray-600 focus:ring-0 cursor-pointer"
                                value={dateFilter.end}
                                onChange={(e) => setDateFilter(prev => ({ ...prev, end: e.target.value }))}
                            />
                        </div>
                    </div>
                </div>

                {/* Счетчик */}
                <div className="md:col-span-1 flex items-center justify-end">
                    <span className="text-[10px] font-black text-slate-400 uppercase tracking-widest text-right">
                        {loading ? '...' : `Найдено:\n${totalCount}`}
                    </span>
                </div>
            </div>

            {/* ТАБЛИЦА СООБЩЕНИЙ */}
            <div className="bg-white rounded-[2.5rem] shadow-sm border border-gray-100 overflow-hidden">
                <table className="w-full text-left border-collapse">
                    <thead>
                    <tr className="bg-gray-50/50 border-b border-gray-100 text-[10px] font-black text-gray-400 uppercase tracking-widest">
                        <th className="p-6 w-px text-center">#</th>
                        <th className="p-6 w-px">Статус</th>
                        <th className="p-6">Trace ID сообщения / Route</th>
                        <th className="p-6">Дата и время поступления</th>
                        <th className="p-6 text-right"></th>
                    </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-50">
                    {loading ? (
                        <tr>
                            <td colSpan="4" className="p-20 text-center text-gray-400 font-bold animate-pulse">
                                ПОЛУЧЕНИЕ ДАННЫХ...
                            </td>
                        </tr>
                    ) : paginatedMessages.length > 0 ? (
                        paginatedMessages.map((msg, idx) => {
                            const displayIndex = (currentPage - 1) * recordsPerPage + idx + 1;
                            const rowKey = msg.traceId || `msg-idx-${idx}`;
                            return (
                                <React.Fragment key={rowKey}>
                                    <tr
                                        onClick={() => toggleRow(msg.traceId)}
                                        className={`group cursor-pointer transition-all 
                                        ${expandedId === msg.traceId
                                            ? 'bg-blue-50/50'
                                            /* Используем displayIndex для сквозного чередования и более контрастный цвет */
                                            : displayIndex % 2 === 0 ? 'bg-blue-100/30' : 'bg-white'} 
                                        hover:bg-blue-100/70`}
                                    >
                                        <td className="p-6 text-[10px] font-bold text-slate-300 text-center">
                                            {displayIndex}
                                        </td>

                                        <td className="px-4 py-3 w-28">
                                            <div className={`inline-flex items-center gap-1.5 px-2 py-1 rounded-full text-[9px] font-black uppercase tracking-tighter
            ${msg.status === 'SUCCESS'
                                                ? 'bg-emerald-100/50 text-emerald-600'
                                                : 'bg-rose-100/50 text-rose-600'}`}>
                                                <div className={`w-1.5 h-1.5 rounded-full ${msg.status === 'SUCCESS' ? 'bg-emerald-500' : 'bg-rose-500'}`} />
                                                {msg.status}
                                            </div>
                                        </td>
                                        <td className="p-6">
                                            <div className="flex flex-col">
                                                <span className="text-[11px] font-mono font-bold text-blue-600 uppercase mb-1 tracking-tight">{msg.traceId || 'NO ID'}</span>
                                                <div className="flex items-center gap-2 text-gray-700 font-black text-xs uppercase tracking-tighter">
                                                    <MessageSquare size={12} className="text-gray-400" />
                                                    <span className="group-hover:underline decoration-blue-500/50 underline-offset-4">
                                                        {msg.routeName || 'UNKNOWN_ROUTE'}
                                                    </span>
                                                </div>
                                            </div>
                                        </td>
                                        <td className="p-6">
                                            <div className="text-[11px] text-gray-400">{msg.createdAt ? new Date(msg.createdAt).toLocaleDateString() : '-'}</div>
                                            <div className="text-xs font-bold text-gray-700">{msg.createdAt ? new Date(msg.createdAt).toLocaleTimeString() : '-'}</div>
                                        </td>
                                        <td className="px-4 py-3 text-right">
                                            <div className={`inline-flex items-center justify-center w-6 h-6 rounded-lg transition-all 
            ${expandedId === msg.traceId ? 'bg-blue-600 text-white shadow-lg shadow-blue-200' : 'bg-slate-100 text-slate-400 group-hover:bg-slate-200'}`}>
                                                <ChevronDown size={14} className={`transition-transform duration-300 ${expandedId === msg.traceId ? 'rotate-180' : ''}`} />
                                            </div>
                                        </td>
                                    </tr>

                                    {expandedId === msg.traceId && (
                                        <tr key={`${rowKey}-details`}>
                                            <td colSpan="5" className="p-0 border-b border-blue-100">
                                                <div className="px-6 py-4 bg-gradient-to-b from-blue-50/20 to-transparent">
                                                    <div className="mb-4 flex items-center gap-2">
                                                        <ArrowRight size={14} className="text-blue-500" />
                                                        <h4 className="text-[10px] font-black text-blue-500 uppercase tracking-[0.2em]">История прохождения шагов</h4>
                                                    </div>
                                                    <AuditTable steps={traceSteps[msg.traceId] || []} />
                                                    {!traceSteps[msg.traceId] && <div className="text-[10px] animate-pulse text-blue-400">Загрузка шагов...</div>}
                                                </div>
                                            </td>
                                        </tr>
                                    )}
                                </React.Fragment>
                            );
                        })
                    ) : (
                        <tr>
                            <td colSpan="4" className="p-10 text-center text-gray-400 text-xs uppercase font-bold">Ничего не найдено</td>
                        </tr>
                    )}
                    </tbody>
                </table>

                {/* ПАГИНАЦИЯ */}
                {totalPages > 1 && (
                    <div className="px-6 py-4 bg-gray-50/50 border-t border-gray-100 flex items-center justify-between">
                        <div className="text-[10px] font-black text-gray-400 uppercase tracking-widest">
                            Страница {currentPage} из {totalPages}
                        </div>
                        <div className="flex items-center gap-2">
                            <button
                                onClick={() => setCurrentPage(prev => Math.max(prev - 1, 1))}
                                disabled={currentPage === 1}
                                className="p-2 rounded-xl bg-white border border-gray-200 text-gray-400 disabled:opacity-30 hover:bg-gray-50 transition-colors"
                            >
                                <ChevronLeft size={18} />
                            </button>
                            <button
                                onClick={() => setCurrentPage(prev => Math.min(prev + 1, totalPages))}
                                disabled={currentPage === totalPages}
                                className="p-2 rounded-xl bg-white border border-gray-200 text-gray-400 disabled:opacity-30 hover:bg-gray-50 transition-colors"
                            >
                                <ChevronRight size={18} />
                            </button>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default MessageAudit;