import { useState, useCallback, useMemo, useRef, useEffect } from 'react';
import { addEdge, applyNodeChanges, applyEdgeChanges, MarkerType } from 'reactflow';
import { NODE_METADATA } from '../constants/nodeMetadata.js';
import { toast } from 'react-hot-toast';

export const useFlowLogic = (initialRouteId) => {
    const [nodes, setNodes] = useState([]);
    const [links, setLinks] = useState([]);
    const [routeName, setRouteName] = useState("MyNewRoute");
    const [routeId, setRouteId] = useState(null);
    const [selectedNodeId, setSelectedNodeId] = useState(null);
    const [selectedLinkId, setSelectedLinkId] = useState(null);
    const API_BASE = "http://localhost:8080/api/admin/routes";
    const [availableRoutes, setAvailableRoutes] = useState([]);
    const reconnectingEdgeId = useRef(null);
    const [isActive, setIsActive] = useState(false);



    const fullConfigJson = useMemo(() => {
        return JSON.stringify({
            ...(routeId && { id: routeId }),
            name: routeName,
            active: isActive,
            nodes: nodes.map(n => {
                const meta = NODE_METADATA[n.data.type] || {};
                const currentConfig = n.data.config || {};

                // 1. Сначала определяем видимые поля и их значения
                const finalConfig = {};

                if (meta.fields) {
                    meta.fields.forEach(f => {
                        // Проверяем условие showIf.
                        // Если условия нет — поле видимо всегда.
                        // Если условие есть — передаем в него текущий конфиг ноды.
                        const isVisible = f.showIf ? f.showIf(currentConfig) : true;

                        if (isVisible) {
                            // Берем значение из конфига, если его нет — берем default
                            const value = currentConfig[f.name] !== undefined
                                ? currentConfig[f.name]
                                : f.default;

                            // Добавляем в объект только если значение существует
                            if (value !== undefined) {
                                finalConfig[f.name] = value;
                            }
                        }
                    });
                }

                // 2. Отделяем технический флаг старта
                const isStart = currentConfig.isStart || false;

                return {
                    id: Number(n.id),
                    type: n.data.type.toUpperCase(),
                    start: !!isStart,
                    config: finalConfig
                };
            }),
            links: links.map(l => {
                const mappingArray = Array.isArray(l.data?.fieldMapping) ? l.data.fieldMapping : [];
                const mappingObject = mappingArray.reduce((acc, item) => {
                    if (item.sourceField?.trim()) acc[item.sourceField] = item.targetField;
                    return acc;
                }, {});
                return {
                    fromNodeId: Number(l.source),
                    toNodeId: Number(l.target),
                    fieldMapping: mappingObject
                };
            })
        }, null, 2);
    }, [nodes, links, routeName, isActive]);

    const fetchRoutes = async () => {
        try {
            const response = await fetch(API_BASE);
            if (!response.ok) throw new Error("Ошибка загрузки");
            const data = await response.json();
            setAvailableRoutes(data);
        } catch (e) {
            toast.error(e.message);
        }
    };

    const deleteRouteFromDb = async (id) => {
        try {
            const response = await fetch(`${API_BASE}/${id}`, { method: 'DELETE' });
            if (!response.ok) throw new Error("Ошибка удаления");
            setAvailableRoutes(prev => prev.filter(r => r.id !== id));
            toast.success("Маршрут удален");
        } catch (e) {
            toast.error(e.message);
        }
    };

    const onNodesChange = useCallback((changes) => setNodes((nds) => applyNodeChanges(changes, nds)), []);
    const onLinksChange = useCallback((changes) => setLinks((ls) => applyEdgeChanges(changes, ls)), []);
    const onConnect = useCallback((params) => {
        setLinks((eds) => {
            // Проверка на дубликат или обратную связь
            const hasConnection = eds.some(
                (e) =>
                    (e.source === params.source && e.target === params.target) ||
                    (e.source === params.target && e.target === params.source)
            );

            if (hasConnection) {
                toast.error("Между этими узлами уже существует связь");
                return eds;
            }

            const edgeId = `e${params.source}-${params.target}`;

            const edgeOptions = {
                ...params,
                id: edgeId,
                type: 'step',
                markerEnd: { type: MarkerType.ArrowClosed, width: 10, height: 20 },
                style: { strokeWidth: 2, stroke: '#cbd5e1' },
                data: { fieldMapping: [{ sourceField: "", targetField: "" }] }
            };
            return addEdge(edgeOptions, eds);
        });
    }, [setLinks]);

    const edgeReconnectSuccessful = useRef(true);

    const onReconnectStart = useCallback((_, edge) => {
        reconnectingEdgeId.current = edge.id; // Запоминаем, какую связь тянем
        edgeReconnectSuccessful.current = false;
    }, []);

    const onReconnect = useCallback((oldEdge, newConnection) => {
        edgeReconnectSuccessful.current = true;
        setLinks((els) => {
            // Убираем старую связь и добавляем новую с правильным ID и хендлами
            const filtered = els.filter((e) => e.id !== oldEdge.id);

            const newEdge = {
                ...oldEdge, // сохраняем маппинг полей
                id: `e${newConnection.source}-${newConnection.target}`,
                source: newConnection.source,
                target: newConnection.target,
                sourceHandle: newConnection.sourceHandle,
                targetHandle: newConnection.targetHandle
            };

            return [...filtered, newEdge];
        });
    }, [setLinks]);

    const onReconnectEnd = useCallback((_, edge) => {
        if (!edgeReconnectSuccessful.current) {
            setLinks((eds) => eds.filter((e) => e.id !== edge.id));
        }
        reconnectingEdgeId.current = null; // Сбрасываем ID
        edgeReconnectSuccessful.current = true;
    }, [setLinks]);

    const addNode = (type) => {
        // Находим все существующие ID, превращаем их в числа и сортируем
        const existingIds = nodes
            .map(n => parseInt(n.id))
            .filter(id => !isNaN(id))
            .sort((a, b) => a - b);

        let newId = 1;
        for (let id of existingIds) {
            if (id === newId) {
                newId++;
            } else if (id > newId) {
                break;
            }
        }

        const newNode = {
            id: String(newId), // ReactFlow требует строковый ID
            type: 'service',
            position: { x: 150, y: 150 },
            data: {
                type: type,
                // Используем label из метаданных или название типа
                label: type === 'START_POINT' ? 'START' : (NODE_METADATA[type]?.title || type),
                config: {}
            },
        };

        setNodes((nds) => nds.concat(newNode));
    };

    const isValidConnection = useCallback((connection) => {
        // 1. Правило "сам с собой"
        if (connection.source === connection.target) return false;

        // 2. Ищем любую связь между этими двумя нодами (в обе стороны)
        const existingEdge = links.find(
            (l) =>
                (l.source === connection.source && l.target === connection.target) ||
                (l.source === connection.target && l.target === connection.source)
        );

        // 3. Если связь существует...
        if (existingEdge) {
            // ...разрешаем только если это реконнект ТОЙ ЖЕ САМОЙ связи
            return existingEdge.id === reconnectingEdgeId.current;
        }

        return true;
    }, [links]);

    const updateMappingRow = (index, key, value) => {
        setLinks((ls) => ls.map((l) => l.id === selectedLinkId ?
            { ...l, data: { ...l.data, fieldMapping: l.data.fieldMapping.map((row, i) => i === index ? { ...row, [key]: value } : row) } } : l));
    };

    const saveVisualMetadata = async (id) => {
        const visualData = {
            nodes: nodes.map(n => ({
                id: n.id,
                position: n.position
            })),
            links: links.map(l => ({
                id: l.id,
                sourceHandle: l.sourceHandle,
                targetHandle: l.targetHandle
            }))
        };

        return fetch(`${API_BASE}/${id}/metadata`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(visualData)
        });
    };

    const saveToDb = async () => {
        const config = JSON.parse(fullConfigJson);
        const method = routeId ? 'PUT' : 'POST';
        const url = routeId ? `${API_BASE}/${routeId}` : API_BASE;

        // Создаем промис для запроса
        const savePromise = fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(config)
        }).then(async (response) => {
            if (!response.ok) throw new Error(`Ошибка: ${response.status}`);
            const data = await response.json();

            // --- НОВОЕ: Сохраняем визуальное состояние ---
            await saveVisualMetadata(data.id);

            return data;
        });

        // Красивое визуальное сопровождение
        toast.promise(savePromise, {
            loading: 'Сохранение маршрута...',
            success: (data) => {
                setRouteId(data.id);
                return `Маршрут "${data.name}" сохранен!`;
            },
            error: (err) => `${err.message}`,
        }, {
            style: {
                minWidth: '250px',
                borderRadius: '12px',
                background: '#1e293b',
                color: '#fff',
                fontSize: '14px'
            },
            success: {
                duration: 4000,
                iconTheme: { primary: '#10b981', secondary: '#fff' },
            },
            error: {
                duration: 6000,
                iconTheme: { primary: '#ef4444', secondary: '#fff' },
            },
        });
    };

    const loadRoute = async (routeConfig) => {
        setRouteId(routeConfig.id);
        setRouteName(routeConfig.name);
        setIsActive(routeConfig.active || false);

        let visualData = null;
        try {
            const res = await fetch(`${API_BASE}/${routeConfig.id}/metadata`);
            if (res.ok) visualData = await res.json();
        } catch {
            console.error("Метаданные не найдены, используем дефолтную раскладку");
        }

        // Восстанавливаем ноды с учетом метаданных
        const restoredNodes = routeConfig.nodes.map(n => {
            const savedPos = visualData?.nodes?.find(v => v.id === String(n.id))?.position;

            return {
                id: String(n.id),
                type: 'service',
                // Если есть в базе — берем координаты, если нет — рандом
                position: savedPos || { x: Math.random() * 400, y: Math.random() * 400 },
                data: {
                    type: n.type,
                    label: NODE_METADATA[n.type]?.title || n.type,
                    config: { ...n.config, isStart: n.start }
                }
            };
        });

        // 3. Восстанавливаем связи
        const restoredLinks = routeConfig.links.map((l) => {
            const edgeId = `e${l.fromNodeId}-${l.toNodeId}`;
            const savedLink = visualData?.links?.find(v => v.id === edgeId);

            return {
                id: edgeId,
                source: String(l.fromNodeId),
                target: String(l.toNodeId),
                // ВОССТАНАВЛИВАЕМ СТОРОНЫ КОННЕКТА
                sourceHandle: savedLink?.sourceHandle || null,
                targetHandle: savedLink?.targetHandle || null,
                type: 'step',
                markerEnd: { type: MarkerType.ArrowClosed, width: 10, height: 20 },
                style: { strokeWidth: 2, stroke: '#cbd5e1' },
                data: {
                    fieldMapping: Object.entries(l.fieldMapping).map(([k, v]) => ({
                        sourceField: k,
                        targetField: v
                    }))
                }
            };
        });

        setNodes(restoredNodes);
        setLinks(restoredLinks);
        toast.success(`Маршрут "${routeConfig.name}" загружен`);
    };

    useEffect(() => {
        const init = async () => {
            if (initialRouteId) {
                // Если есть ID, загружаем данные с бэкенда
                try {
                    const response = await fetch(`${API_BASE}/${initialRouteId}`);
                    if (!response.ok) throw new Error("Маршрут не найден");
                    const fullRouteData = await response.json();

                    // Используем твою существующую функцию для отрисовки
                    loadRoute(fullRouteData);
                } catch (e) {
                    toast.error("Ошибка при открытии маршрута");
                    console.error(e);
                }
            } else {
                // Если ID нет (создание нового) — сбрасываем всё
                setRouteId(null);
                setRouteName("MyNewRoute");
                setNodes([]);
                setLinks([]);
                setIsActive(false);
            }
        };

        init();
    }, [initialRouteId]);

    return {
        nodes, links, routeName, setRouteName, selectedNodeId, setSelectedNodeId,
        selectedLinkId, setSelectedLinkId, fullConfigJson, onNodesChange,
        onLinksChange, onConnect, addNode, updateMappingRow,
        setNodes, setLinks, isValidConnection, loadRoute, saveToDb, availableRoutes, fetchRoutes,
        deleteRouteFromDb, onReconnect, onReconnectStart, onReconnectEnd, isActive, setIsActive,
        routeId, setRouteId
    };
};