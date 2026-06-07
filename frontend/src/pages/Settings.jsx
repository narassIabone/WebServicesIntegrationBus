import React, { useState, useEffect } from 'react';
import { Save, RefreshCw, Sliders, ArrowRightLeft, AlertCircle, CheckCircle } from 'lucide-react';

const API_BASE_URL = 'http://localhost:8080/api/v1/settings'; // Скорректируйте порт под ваш бэкенд

export default function Settings() {
    const [settings, setSettings] = useState({
        esb_runtime_concurrency: '',
        app_executor_max_retries: '',
        topic_inbound: '',
        topic_error: '',
        topic_final: '',
        topic_unknown: '' // <-- Добавили новое поле в стейт
    });

    const [isLoading, setIsLoading] = useState(true);
    const [isSaving, setIsSaving] = useState(false);
    const [statusMessage, setStatusMessage] = useState(null); // { type: 'success' | 'error', text: '' }

    // Загрузка настроек при инициализации страницы
    useEffect(() => {
        fetchSettings();
    }, []);

    const fetchSettings = async () => {
        setIsLoading(true);
        setStatusMessage(null);
        try {
            const response = await fetch(API_BASE_URL);
            if (!response.ok) throw new Error('Не удалось загрузить настройки');
            const data = await response.json();
            setSettings(data);
        } catch (error) {
            setStatusMessage({ type: 'error', text: error.message || 'Ошибка соединения с сервером' });
        } finally {
            setIsLoading(false);
        }
    };

    const handleInputChange = (e) => {
        const { name, value, type } = e.target;
        setSettings(prev => ({
            ...prev,
            [name]: type === 'number' ? parseInt(value, 10) || 0 : value
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsSaving(true);
        setStatusMessage(null);

        try {
            const response = await fetch(API_BASE_URL, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(settings)
            });

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText || 'Ошибка при сохранении настроек');
            }

            setStatusMessage({ type: 'success', text: 'Настройки успешно сохранены и применены!' });
        } catch (error) {
            setStatusMessage({ type: 'error', text: error.message });
        } finally {
            setIsSaving(false);
        }
    };

    if (isLoading) {
        return (
            <div className="flex h-64 items-center justify-center text-slate-500">
                <RefreshCw className="mr-2 h-5 w-5 animate-spin" />
                <span>Загрузка конфигурации рантайма...</span>
            </div>
        );
    }

    return (
        <div className="mx-auto max-w-4xl p-6">
            <div className="mb-6 flex items-center justify-between border-b border-slate-200 pb-4">
                <div>
                    <h1 className="text-2xl font-semibold text-slate-800">Управление рантаймом шины</h1>
                    <p className="text-sm text-slate-500">Динамическое изменение конфигурации распределения ресурсов и очередей Кафки</p>
                </div>
                <button
                    onClick={fetchSettings}
                    className="flex items-center gap-1.5 rounded-lg border border-slate-300 bg-white px-3 pb-2 pt-2 text-sm font-medium text-slate-700 shadow-sm transition hover:bg-slate-50"
                    title="Обновить данные из БД"
                >
                    <RefreshCw className="h-4 w-4" />
                    Синхронизировать
                </button>
            </div>

            {/* Блок уведомлений */}
            {statusMessage && (
                <div className={`mb-6 flex items-start gap-3 rounded-lg p-4 text-sm ${
                    statusMessage.type === 'success'
                        ? 'bg-emerald-50 border border-emerald-200 text-emerald-800'
                        : 'bg-rose-50 border border-rose-200 text-rose-800'
                }`}>
                    {statusMessage.type === 'success' ? (
                        <CheckCircle className="h-5 w-5 shrink-0 text-emerald-600" />
                    ) : (
                        <AlertCircle className="h-5 w-5 shrink-0 text-rose-600" />
                    )}
                    <span className="font-medium">{statusMessage.text}</span>
                </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-6">

                {/* Секция 1: Потоки и Производительность */}
                <div className="rounded-xl border border-slate-200 bg-white shadow-sm">
                    <div className="flex items-center gap-2 border-b border-slate-100 px-5 py-4 font-medium text-slate-800 bg-slate-50/50 rounded-t-xl">
                        <Sliders className="h-4 w-4 text-indigo-500" />
                        <h2>Производительность и отказоустойчивость</h2>
                    </div>
                    <div className="p-5 space-y-4">
                        <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
                            <div>
                                <label className="block text-sm font-medium text-slate-700">
                                    Параллельные потоки (Concurrency)
                                </label>
                                <input
                                    type="number"
                                    name="esb_runtime_concurrency"
                                    min="1"
                                    max="50"
                                    value={settings.esb_runtime_concurrency}
                                    onChange={handleInputChange}
                                    className="mt-1 block w-full rounded-lg border border-slate-300 px-3 py-2 shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500 sm:text-sm"
                                />
                                <p className="mt-1 text-xs text-slate-400">Количество параллельных обработчиков Kafka Listener Container. Влияет на скорость обработки сообщений. Значение не должно превышать количество партиций в топике</p>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-slate-700">
                                    Максимум ретраев (Max Retries)
                                </label>
                                <input
                                    type="number"
                                    name="app_executor_max_retries"
                                    min="0"
                                    max="10"
                                    value={settings.app_executor_max_retries}
                                    onChange={handleInputChange}
                                    className="mt-1 block w-full rounded-lg border border-slate-300 px-3 py-2 shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500 sm:text-sm"
                                />
                                <p className="mt-1 text-xs text-slate-400">Количество попыток повторной обработки сообщения при возникновении восстановимых (Retryable) ошибок</p>
                            </div>
                        </div>
                    </div>
                </div>

                <div className="rounded-xl border border-slate-200 bg-white shadow-sm">
                    <div className="flex items-center gap-2 border-b border-slate-100 px-5 py-4 font-medium text-slate-800 bg-slate-50/50 rounded-t-xl">
                        <ArrowRightLeft className="h-4 w-4 text-emerald-500" />
                        <h2>Глобальные топики шины интеграции</h2>
                    </div>
                    <div className="p-5">
                        {/* Общая сетка 2х2 для всех полей */}
                        <div className="grid grid-cols-1 gap-6 md:grid-cols-2">

                            {/* 1. Входящий топик */}
                            <div>
                                <label className="block text-sm font-medium text-slate-700">
                                    Входящий топик (Inbound Topic)
                                    <span className="ml-2 inline-flex items-center rounded bg-amber-50 px-1.5 py-0.5 text-xs font-medium text-amber-800 border border-amber-200">⚡ Требует рестарта шины</span>
                                </label>
                                <input
                                    type="text"
                                    name="topic_inbound"
                                    value={settings.topic_inbound}
                                    onChange={handleInputChange}
                                    className="mt-1 block w-full rounded-lg border border-slate-300 px-3 py-2 shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500 sm:text-sm"
                                />
                                <p className="mt-1 text-xs text-slate-400">Топик, с которого начинается любой маршрут</p>
                            </div>

                            {/* 2. Топик системных ошибок */}
                            <div>
                                <label className="block text-sm font-medium text-slate-700">
                                    Топик системных ошибок (DLQ)
                                </label>
                                <input
                                    type="text"
                                    name="topic_error"
                                    value={settings.topic_error}
                                    onChange={handleInputChange}
                                    className="mt-1 block w-full rounded-lg border border-slate-300 px-3 py-2 shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500 sm:text-sm"
                                />
                                <p className="mt-1 text-xs text-slate-400">Топик для сообщений, вызвавших критическую ошибку в процессе выполнения шагов</p>
                            </div>

                            {/* 3. Финальный топик доставок */}
                            <div>
                                <label className="block text-sm font-medium text-slate-700">
                                    Финальный топик доставок
                                </label>
                                <input
                                    type="text"
                                    name="topic_final"
                                    value={settings.topic_final}
                                    onChange={handleInputChange}
                                    className="mt-1 block w-full rounded-lg border border-slate-300 px-3 py-2 shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500 sm:text-sm"
                                />
                                <p className="mt-1 text-xs text-slate-400">Топик для сообщений, успешно прошедших всю цепочку графа маршрутизации</p>
                            </div>

                            {/* 4. Топик неизвестных маршрутов */}
                            <div>
                                <label className="block text-sm font-medium text-slate-700">
                                    Топик неизвестных маршрутов
                                </label>
                                <input
                                    type="text"
                                    name="topic_unknown"
                                    value={settings.topic_unknown}
                                    onChange={handleInputChange}
                                    className="mt-1 block w-full rounded-lg border border-slate-300 px-3 py-2 shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500 sm:text-sm"
                                />
                                <p className="mt-1 text-xs text-slate-400">Топик для сообщений, отправленных на несуществующий маршрут</p>
                            </div>

                        </div>
                    </div>
                </div>

                {/* Кнопка отправки формы */}
                <div className="flex justify-end">
                    <button
                        type="submit"
                        disabled={isSaving}
                        className="flex items-center gap-2 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white shadow transition hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 disabled:opacity-60"
                    >
                        {isSaving ? (
                            <RefreshCw className="h-4 w-4 animate-spin" />
                        ) : (
                            <Save className="h-4 w-4" />
                        )}
                        {isSaving ? 'Сохранение...' : 'Применить конфигурацию'}
                    </button>
                </div>
            </form>
        </div>
    );
}