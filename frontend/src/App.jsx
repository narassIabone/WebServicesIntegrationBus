import React, { useState } from 'react';
import { LayoutDashboard, Activity, Settings, Search, Zap } from 'lucide-react';
import { Toaster } from 'react-hot-toast';

// Импортируем наши страницы
import Dashboard from './pages/Dashboard.jsx';
import FlowEditor from './pages/FlowEditor.jsx';
import SettingsPage from './pages/Settings.jsx';
import MessageAudit from './pages/MessageAudit.jsx';
import RouteList from './pages/RouteList.jsx';

function App() {
    const [activeTab, setActiveTab] = useState('routes');

    const [editingRouteId, setEditingRouteId] = useState(null);

    const handleEditRoute = (id) => {
        setEditingRouteId(id);
        setActiveTab('editor');
    };

    // Массив вкладок для удобного управления навигацией и заголовками
    const tabs = [
        {id: 'routes', label: 'Маршруты', icon: <Zap size={20} />, title: 'Управление маршрутами шины'},
        { id: 'dashboard', label: 'Статистика', icon: <LayoutDashboard size={20} />, title: 'Общая статистика системы' },
        { id: 'editor', label: 'Конструктор', icon: <Activity size={20} />, title: 'Конструктор маршрутов' },
        { id: 'audit', label: 'Аудит', icon: <Search size={20} />, title: 'Аудит сообщений и жизненный цикл' },
        { id: 'settings', label: 'Настройки', icon: <Settings size={20} />, title: 'Настройки интеграционной шины' },
    ];

    // Находим текущую активную вкладку для заголовка
    const currentTab = tabs.find(t => t.id === activeTab);

    return (
        <div className="flex h-screen bg-gray-100 font-sans">
            {/* Боковое меню */}
            <div className="w-72 bg-slate-900 text-white flex flex-col shadow-xl">
                <div className="p-6 bg-slate-950">
                    <div className="flex items-center gap-3">
                        <div className="w-8 h-8 bg-blue-500 rounded-lg flex items-center justify-center font-bold text-white">B</div>
                        <span className="text-xl font-bold tracking-tight">ESB Integration</span>
                    </div>
                </div>

                <Toaster position="bottom-right" reverseOrder={false} />

                <nav className="flex-1 px-4 mt-6 space-y-2">
                    {tabs.map((item) => (
                        <button
                            key={item.id}
                            onClick={() => setActiveTab(item.id)}
                            className={`w-full flex items-center px-4 py-3 rounded-xl transition-all duration-200 ${
                                activeTab === item.id
                                    ? 'bg-blue-600 text-white shadow-lg shadow-blue-900/50'
                                    : 'text-slate-400 hover:bg-slate-800 hover:text-white'
                            }`}
                        >
                            {item.icon}
                            <span className="ml-3 font-medium">{item.label}</span>
                        </button>
                    ))}
                </nav>
            </div>

            {/* Основная область */}
            <div className="flex-1 flex flex-col overflow-hidden">
                <header className="h-16 bg-white border-b border-gray-200 flex items-center px-8 shadow-sm">
                    <h2 className="text-lg font-semibold text-gray-700">
                        {currentTab ? currentTab.title : 'Загрузка...'}
                    </h2>
                </header>

                <main className="flex-1 overflow-auto p-6">
                    {/* Рендерим нужную страницу в зависимости от стейта */}
                    {activeTab === 'routes' && (
                        <RouteList
                            onEditRoute={handleEditRoute}
                            onCreateNew={() => {
                                setEditingRouteId(null);
                                setActiveTab('editor');
                            }}
                        />
                    )}

                    {activeTab === 'dashboard' && <Dashboard />}

                    {activeTab === 'editor' && (
                        <div className="bg-white rounded-xl shadow-lg border border-gray-200 overflow-hidden h-full">
                            <FlowEditor routeId={editingRouteId} />
                        </div>
                    )}

                    {activeTab === 'audit' && <MessageAudit />}

                    {activeTab === 'settings' && <SettingsPage />}
                </main>
            </div>
        </div>
    );
}

export default App;