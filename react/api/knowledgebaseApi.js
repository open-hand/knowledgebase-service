import { getProjectId, request, getOrganizationId } from '../common/utils';

export const getBaseInfo = (baseId) => request.get(`/agile/v1/projects/${getProjectId()}/product_version/versions`);

export const createBase = () => {

};

export const editBase = () => {
    
};
