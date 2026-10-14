import React, { useEffect, useState } from 'react';
import {
    BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
    AreaChart, Area, Cell, PieChart, Pie, Legend
} from 'recharts';
import { Activity, CheckCircle2, Clock, RefreshCcw, Zap, ShieldAlert, ListTree, Calendar} from 'lucide-react';
import { toast } from 'react-hot-toast';
import IncidentsTable from '../components/ui/IncidentsTable.jsx';
import CustomSelect from '../components/ui/CustomSelect';

// Компонент карточки метрики
const StatCard = ({ title, value, icon: Icon, color, subtext }) => (
    <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100 flex items-start justify-between">
        <div>
            <p className="text-sm font-medium text-gray-500 mb-1">{title}</p>
            <h3 className="text-2xl font-bold text-gray-900">{value}</h3>
            {subtext && <p className="text-xs text-gray-400 mt-1">{subtext}</p>}
        </div>
        <div className={`p-3 rounded-xl ${color}`}>
            <Icon size={24} className="text-white" />
        </div>
    </div>
);

const Dashboard = () => {
    const TIME_RANGES = [
        { label: 'За последний час', value: 'hour' },
        { label: 'За последний день', value: 'day' },
        { label: 'За последнюю неделю', value: 'week' },
        { label: 'За последний месяц', value: 'month' },
        { label: 'За всё время', value: 'all' },
        { label: 'Свой период', value: 'custom' },
    ];

    const [range, setRange] = useState('day');
    const [customDates, setCustomDates] = useState({ start: '', end: '' });
    const [data, setData] = useState({
        summary: { total: 0, success: 0, errors: 0 },
        avgLatency: 0,
        hourlyStats: [],
        nodeErrors: [],
        incidents: [],
        routeAnalytics: []
    });
    const [loading, setLoading] = useState(true);

    const API_BASE = 'http://localhost:8080/api/admin/audit';

    const fetchDashboardData = async () => {
        try {
            const params = new URLSearchParams({ range });
            if (range === 'custom') {
                params.append('start', customDates.start);
                params.append('end', customDates.end);
            }

            const queryString = `?${params.toString()}`;

            const [detailsRes, incidentsRes, routesRes] = await Promise.all([
                fetch(`${API_BASE}/stats/details${queryString}`),
                fetch(`${API_BASE}/incidents${queryString}`),
                fetch(`${API_BASE}/stats/routes${queryString}`)
            ]);

            if (!detailsRes.ok || !incidentsRes.ok || !routesRes.ok) throw new Error('API Error');
            const details = await detailsRes.json();
            const incidents = await incidentsRes.json();
            const routes = await routesRes.json();

            const formattedHourly = details.hourlyStats.map(item => ({
                time: item.time,
                success: item.success,
                errors: item.errors
            }));

            if (range === 'custom') {
                const formatDateForGraph = (dateString) => {
                    const d = new Date(dateString);
                    if (isNaN(d.getTime())) return null;
                    const day = String(d.getDate()).padStart(2, '0');
                    const month = String(d.getMonth() + 1).padStart(2, '0');
                    const hours = String(d.getHours()).padStart(2, '0');
                    const minutes = String(d.getMinutes()).padStart(2, '0');
                    return `${day}.${month} ${hours}:${minutes}`;
                };

                const formattedStart = customDates.start ? formatDateForGraph(customDates.start) : null;
                const formattedEnd = customDates.end ? formatDateForGraph(customDates.end) : null;

                if (formattedHourly.length === 0) {
                    // Если логов вообще нет за период, просто рисуем пустую прямую от старта до конца
                    if (formattedStart) formattedHourly.push({ time: formattedStart, success: 0, errors: 0 });
                    if (formattedEnd && formattedEnd !== formattedStart) formattedHourly.push({ time: formattedEnd, success: 0, errors: 0 });
                } else {
                    // Добавляем точку старта в начало, если её там нет
                    if (formattedStart && formattedHourly[0].time !== formattedStart) {
                        formattedHourly.unshift({
                            time: formattedStart,
                            success: 0,
                            errors: 0
                        });
                    }
                    // Добавляем точку финиша в конец, если её там нет
                    if (formattedEnd && formattedHourly[formattedHourly.length - 1].time !== formattedEnd) {
                        formattedHourly.push({
                            time: formattedEnd,
                            success: 0,
                            errors: 0
                        });
                    }
                }
            }

            const formattedNodeErrors = details.nodeErrors.map(item => ({
                name: item[0],
                errors: item[1]
            }));

            setData({
                summary: details.summary,
                avgLatency: details.avgLatency || 0,
                hourlyStats: formattedHourly,
                nodeErrors: formattedNodeErrors,
                incidents: incidents,
                routeAnalytics: routes
            });
        } catch {
            toast.error("Ошибка синхронизации данных");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchDashboardData();
        const interval = setInterval(fetchDashboardData, 15000);
        return () => clearInterval(interval);
    }, [range, customDates]);

    if (loading && data.summary.total === 0) {
        return (
            <div className="flex flex-col items-center justify-center h-full text-slate-400">
                <RefreshCcw className="animate-spin mb-4" size={48} />
                <p className="text-lg font-medium">Сбор метрик из базы данных...</p>
            </div>
        );
    }

    const totalPoints = data.hourlyStats.length;
    const axisInterval = totalPoints > 10 ? Math.floor(totalPoints / 10) - 1 : 0;

    return (
        <div className="space-y-8 animate-in fade-in duration-500 pb-12">
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-6">
                <h2 className="text-xl font-bold text-gray-800">Системная аналитика</h2>

                <div className="flex flex-wrap items-center gap-3 bg-white p-2 rounded-xl border border-gray-100 shadow-sm">
                    <div className="w-60"> {/* Ограничим ширину, чтобы селект не растягивался */}
                        <CustomSelect
                            value={range}
                            onChange={(selected) => setRange(selected)}
                            // Передаем только технические ключи (value)
                            options={TIME_RANGES.map(r => r.value)}
                            // Функция, которая превратит '1h' в 'Последний час' для отображения
                            labelTransformer={(val) => TIME_RANGES.find(r => r.value === val)?.label || val}
                            icon={Calendar}
                            placeholder="Выберите период"
                        />
                    </div>

                    {range === 'custom' && (
                        <div className="flex items-center gap-2 border-l pl-3 animate-in slide-in-from-left-2">
                            <input
                                type="datetime-local"
                                className="text-xs border-gray-200 rounded-lg focus:ring-blue-500"
                                onChange={(e) => setCustomDates(prev => ({...prev, start: e.target.value}))}
                            />
                            <span className="text-gray-400">—</span>
                            <input
                                type="datetime-local"
                                className="text-xs border-gray-200 rounded-lg focus:ring-blue-500"
                                onChange={(e) => setCustomDates(prev => ({...prev, end: e.target.value}))}
                            />
                        </div>
                    )}
                </div>
            </div>
            {/* ВЕРХНИЕ КАРТОЧКИ */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                <StatCard
                    title="Всего сообщений"
                    value={data.summary.total.toLocaleString()}
                    icon={Activity}
                    color="bg-blue-500"
                    subtext="За выбранный период"
                />
                <StatCard
                    title="Здоровье системы"
                    value={`${((data.summary.success / (data.summary.total || 1)) * 100).toFixed(1)}%`}
                    icon={CheckCircle2}
                    color="bg-emerald-500"
                    subtext={`${data.summary.success} успешно`}
                />
                <StatCard
                    title="Ошибки"
                    value={data.summary.errors.toLocaleString()}
                    icon={ShieldAlert}
                    color="bg-rose-500"
                    subtext="Требуют внимания"
                />
                <StatCard
                    title="Latency (AVG)"
                    value={
                        data.avgLatency >= 1000
                            ? `${(data.avgLatency / 1000).toFixed(2)} с`
                            : `${data.avgLatency.toFixed(0)} мс`
                    }
                    icon={Clock}
                    color="bg-amber-500"
                    subtext="Среднее время обработки сообщения маршрутом"
                />
            </div>

            {/* ГРАФИКИ АКТИВНОСТИ И КОМПОНЕНТОВ */}
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">

                <div className="lg:col-span-2 bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
                    <div className="flex items-center justify-between mb-6">
                        <h3 className="text-lg font-bold text-gray-800">Нагрузка на шину</h3>
                        <div className="flex items-center gap-4 text-xs font-medium">
                            <span className="flex items-center gap-1 text-emerald-600"><div className="w-2 h-2 rounded-full bg-emerald-500" /> Success</span>
                            <span className="flex items-center gap-1 text-rose-600"><div className="w-2 h-2 rounded-full bg-rose-500" /> Errors</span>
                        </div>
                    </div>
                    <div className="h-80 w-full">
                        <ResponsiveContainer width="100%" height="100%">
                            <AreaChart data={data.hourlyStats}>
                                <defs>
                                    <linearGradient id="colorSucc" x1="0" y1="0" x2="0" y2="1">
                                        <stop offset="5%" stopColor="#10b981" stopOpacity={0.1}/>
                                        <stop offset="95%" stopColor="#10b981" stopOpacity={0}/>
                                    </linearGradient>
                                </defs>
                                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />

                                {/* ИЗМЕНИЛИ ТУТ: Добавлен фиксированный интервал отрисовки шагов оси */}
                                <XAxis
                                    dataKey="time"
                                    axisLine={false}
                                    tickLine={false}
                                    tick={{fill: '#94a3b8', fontSize: 11}}
                                    interval={axisInterval} // Задает строгую плотность шагов
                                    minTickGap={10} // Снизили зазор, чтобы Recharts не выкидывал принудительные тики
                                />

                                <YAxis axisLine={false} tickLine={false} tick={{fill: '#94a3b8', fontSize: 11}} />
                                <Tooltip
                                    content={({ active, payload, label }) => {
                                        if (active && payload && payload.length) {
                                            return (
                                                <div className="bg-white p-3 rounded-xl shadow-xl border border-gray-50">
                                                    <p className="text-xs font-bold text-gray-400 mb-2">{label}</p>
                                                    <div className="space-y-1">
                                                        <p className="text-sm font-semibold text-emerald-600">
                                                            Успешно: {payload[0].value}
                                                        </p>
                                                        <p className="text-sm font-semibold text-rose-600">
                                                            Ошибки: {payload[1].value}
                                                        </p>
                                                    </div>
                                                </div>
                                            );
                                        }
                                        return null;
                                    }}
                                />
                                <Area type="monotone" dataKey="success" stroke="#10b981" fillOpacity={1} fill="url(#colorSucc)" strokeWidth={3} />
                                <Area type="monotone" dataKey="errors" stroke="#f43f5e" fill="none" strokeWidth={2} strokeDasharray="5 5" />
                            </AreaChart>
                        </ResponsiveContainer>
                    </div>
                </div>

                <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
                    <h3 className="text-lg font-bold text-gray-800 mb-6 flex justify-between items-center">
                        Сбои по типам узлов
                        <span className="text-xs font-normal text-gray-400">Топ за период</span>
                    </h3>
                    <div className="h-80 w-full">
                        {data.nodeErrors && data.nodeErrors.length > 0 ? (
                            <ResponsiveContainer width="100%" height="100%">
                                <BarChart
                                    data={data.nodeErrors}
                                    layout="vertical"
                                    margin={{ left: 20, right: 40 }}
                                >
                                    <XAxis type="number" hide />
                                    <YAxis
                                        dataKey="name"
                                        type="category"
                                        axisLine={false}
                                        tickLine={false}
                                        tick={{fontSize: 12, fontWeight: 700, fill: '#64748b'}}
                                        width={100}
                                    />
                                    <Tooltip
                                        cursor={{fill: '#f8fafc'}}
                                        contentStyle={{ borderRadius: '12px', border: 'none', boxShadow: '0 10px 15px -3px rgba(0,0,0,0.1)' }}
                                    />
                                    <Bar
                                        dataKey="errors"
                                        fill="#f43f5e"
                                        radius={[0, 6, 6, 0]}
                                        barSize={24}
                                        label={{
                                            position: 'right',
                                            fill: '#f43f5e',
                                            fontSize: 12,
                                            fontWeight: 'bold',
                                            formatter: (value) => value > 0 ? `${value} шт.` : ''
                                        }}
                                    >
                                        {data.nodeErrors.map((entry, index) => (
                                            <Cell key={`cell-${index}`} fillOpacity={1 - (index * 0.15)} />
                                        ))}
                                    </Bar>
                                </BarChart>
                            </ResponsiveContainer>
                        ) : (
                            // Заглушка, если массив пустой или не пришел
                            <div className="flex flex-col items-center justify-center h-full text-slate-400 space-y-1 animate-in fade-in duration-300">
                                <p className="text-sm font-semibold text-slate-500">Сбоев нет</p>
                                <p className="text-xs text-slate-400">За выбранный период ошибок на узлах не зафиксировано</p>
                            </div>
                        )}
                    </div>
                </div>
            </div>

            {/* АНАЛИТИКА ПО МАРШРУТАМ (НОВАЯ СЕКЦИЯ) */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                {/* Распределение трафика */}
                <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
                    <h3 className="text-lg font-bold text-gray-800 mb-6 flex items-center gap-2">
                        <Zap size={20} className="text-amber-500" /> Доля трафика по маршрутам
                    </h3>
                    <div className="h-72">
                        <ResponsiveContainer width="100%" height="100%">
                            <PieChart>
                                <Pie
                                    data={data.routeAnalytics}
                                    dataKey="count"
                                    nameKey="name"
                                    cx="50%"
                                    cy="50%"
                                    innerRadius={70}
                                    outerRadius={90}
                                    paddingAngle={8}
                                >
                                    {data.routeAnalytics.map((_, index) => (
                                        <Cell key={`cell-${index}`} fill={['#3b82f6', '#10b981', '#f59e0b', '#8b5cf6', '#ec4899'][index % 5]} />
                                    ))}
                                </Pie>
                                <Tooltip />
                                <Legend verticalAlign="bottom" height={36}/>
                            </PieChart>
                        </ResponsiveContainer>
                    </div>
                </div>

                {/* Рейтинг здоровья маршрутов */}
                <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
                    <h3 className="text-lg font-bold text-gray-800 mb-6 flex items-center gap-2">
                        <ListTree size={20} className="text-blue-500" /> Состояние маршрутов
                    </h3>
                    <div className="space-y-5 overflow-y-auto max-h-[280px] pr-2">
                        {data.routeAnalytics.map((route) => (
                            <div key={route.id} className="group">
                                <div className="flex justify-between items-end mb-2">
                                    <div>
                                        <span className="text-sm font-bold text-gray-700 block">{route.name}</span>
                                        <span className="text-[10px] text-gray-400 uppercase tracking-wider">{route.count} сообщений · {route.avgLatency}ms avg</span>
                                    </div>
                                    <span className={`text-xs font-bold ${route.health > 90 ? 'text-emerald-500' : 'text-rose-500'}`}>
                                        {route.health}%
                                    </span>
                                </div>
                                <div className="w-full h-2 bg-gray-100 rounded-full overflow-hidden">
                                    <div
                                        className={`h-full transition-all duration-1000 ${route.health > 90 ? 'bg-emerald-500' : route.health > 70 ? 'bg-amber-500' : 'bg-rose-500'}`}
                                        style={{ width: `${route.health}%` }}
                                    />
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            </div>

            {/* ПОСЛЕДНИЕ ИНЦИДЕНТЫ */}
            <IncidentsTable incidents={data.incidents} />

        </div>
    );
};

export default Dashboard;