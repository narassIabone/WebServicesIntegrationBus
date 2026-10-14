import React, {useState} from 'react';
import ReactFlow, { Background, Controls, ReactFlowProvider } from 'reactflow';
import 'reactflow/dist/style.css';
import ServiceNode from '../components/nodes/ServiceNode.jsx';
import ConfigSidebar from '../components/editor/ConfigSidebar.jsx';
import NodeLibrary from '../components/editor/NodeLibrary.jsx';
import RouteListModal from '../components/ui/RouteListModal.jsx';
import Toolbar from '../components/editor/Toolbar.jsx';
import SystemToolbar from '../components/editor/SystemToolbar.jsx';
import { useFlowLogic } from '../hooks/useFlowLogic.js';

const nodeTypes = { service: ServiceNode };

const FlowEditor = ({ routeId }) => (
    <ReactFlowProvider>
        <FlowContent routeId={routeId} />
    </ReactFlowProvider>
);

const FlowContent = ({ routeId }) => {
    const logic = useFlowLogic(routeId);

    const [isModalOpen, setIsModalOpen] = useState(false);

    // Обработчик клика по ноде
    const onNodeClick = (_, node) => {
        logic.setSelectedNodeId(node.id);
        logic.setSelectedLinkId(null);
    };

    // Исправленный обработчик клика по связи (используем logic.)
    const onEdgeClick = (_, edge) => {
        logic.setSelectedLinkId(edge.id);
        logic.setSelectedNodeId(null);
    };

    // Пропсы для сайдбара — используем logic для всех стейтов
    const sidebarProps = {
        routeName: logic.routeName,
        setRouteName: logic.setRouteName,
        selectedNode: logic.nodes.find(n => n.id === logic.selectedNodeId),
        selectedLink: logic.links.find(l => l.id === logic.selectedLinkId),
        fullConfigJson: logic.fullConfigJson,
        updateNodeConfig: (key, value) => logic.setNodes(nds => nds.map(n => n.id === logic.selectedNodeId ? { ...n, data: { ...n.data, config: { ...n.data.config, [key]: value } } } : n)),
        addMappingRow: () => logic.setLinks(ls => ls.map(l => l.id === logic.selectedLinkId ? { ...l, data: { ...l.data, fieldMapping: [...(l.data.fieldMapping || []), { sourceField: "", targetField: "" }] } } : l)),
        updateMappingRow: logic.updateMappingRow,
        removeMappingRow: (idx) => logic.setLinks(ls => ls.map(l => l.id === logic.selectedLinkId ? { ...l, data: { ...l.data, fieldMapping: l.data.fieldMapping.filter((_, i) => i !== idx) } } : l))
    };

    return (
        <div className="flex w-full h-full bg-slate-50 overflow-hidden font-sans">
            <div className="relative flex-1 h-full">

                {/* ЛЕВАЯ ПАНЕЛЬ: Панель инструментов */}
                <NodeLibrary logic={logic} />

                {/* СИСТЕМНАЯ ПАНЕЛЬ (НОВОЕ) */}
                <SystemToolbar
                    logic={logic}
                    onOpenList={() => { logic.fetchRoutes(); setIsModalOpen(true); }}
                />

                {/* ЦЕНТР: Холст */}
                <ReactFlow
                    nodes={logic.nodes}
                    edges={logic.links}
                    onNodesChange={logic.onNodesChange}
                    onEdgesChange={logic.onLinksChange}
                    onNodesDelete={() => logic.setSelectedNodeId(null)}
                    onEdgesDelete={() => logic.setSelectedLinkId(null)}
                    onConnect={logic.onConnect}
                    onNodeClick={onNodeClick}
                    onEdgeClick={onEdgeClick}
                    onPaneClick={() => {
                        logic.setSelectedNodeId(null);
                        logic.setSelectedLinkId(null);
                    }}
                    onReconnect={logic.onReconnect}
                    onReconnectStart={logic.onReconnectStart}
                    onReconnectEnd={logic.onReconnectEnd}
                    isValidConnection={logic.isValidConnection}
                    nodeTypes={nodeTypes}
                    connectionMode="loose"
                    fitView
                    deleteKeyCode="Delete"
                >
                    <Background color="#cbd5e1" gap={24} variant="lines" />
                    <Controls position="bottom-left" style={{ marginBottom: 10 }} />
                </ReactFlow>
            </div>

            {/* НИЖНЯЯ ПАНЕЛЬ: Сохранение и удаление маршрута */}
            <Toolbar logic={logic} onOpenList={() => { logic.fetchRoutes(); setIsModalOpen(true); }} />

            {/* ПРАВАЯ ПАНЕЛЬ: Конфигуратор */}
            <ConfigSidebar {...sidebarProps} />

            {/* МОДАЛЬНОЕ ОКНО: Список маршрутов */}
            <RouteListModal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                routes={logic.availableRoutes}
                onSelect={logic.loadRoute}
                onDelete={logic.deleteRouteFromDb}
            />
        </div>
    );
};

const FlowEditorExport = ({ routeId }) => (
    <ReactFlowProvider>
        <FlowContent routeId={routeId} />
    </ReactFlowProvider>
);

export default FlowEditorExport;